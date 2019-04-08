package com.thoughtmechanix.zuulsvr.filters;


import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.thoughtmechanix.zuulsvr.model.AbTestingRoute;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Implementing a Zuul route filter requires the most coding effort, because with a route filter you’re taking over a
 * core piece of Zuul functionality, routing, and replacing it with your own functionality.
 *
 * The specialroutesservice has a database record for the organization service that will route the requests for calls to
 * the organization service 50% of the time to the existing organization service (the one mapped in Zuul) and 50% of the
 * time to an alternative organization service. The alternative organization service route returned from the SpecialRoutes
 * service will be http://orgservice-new and will not be accessible directly from Zuul.
 */
@Component
public class SpecialRoutesFilter extends ZuulFilter {
    private static final int FILTER_ORDER =  1;
    private static final boolean SHOULD_FILTER =true;

    private static final Logger logger = LoggerFactory.getLogger(SpecialRoutesFilter.class);

    @Autowired
    FilterUtils filterUtils;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public String filterType() {
        return filterUtils.ROUTE_FILTER_TYPE;
    }

    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return SHOULD_FILTER;
    }

    // The helper variable is an instance variable of type ProxyRequestHelper class. This is a Spring Cloud class
    //with helper functions for proxying service requests.
    private ProxyRequestHelper helper = new ProxyRequestHelper();

    private AbTestingRoute getAbRoutingInfo(String serviceName){
        ResponseEntity<AbTestingRoute> restExchange = null;
        try {
            //Calls the SpecialRoutesService endpoint
            restExchange = restTemplate.exchange(
                             "http://specialroutesservice/v1/route/abtesting/{serviceName}",
                             HttpMethod.GET,
                             null, AbTestingRoute.class, serviceName);
        }
        catch(HttpClientErrorException ex){
            if (ex.getStatusCode()== HttpStatus.NOT_FOUND) return null;
            throw ex;
        }
        return restExchange.getBody();
    }

    private String buildRouteString(String oldEndpoint, String newEndpoint, String serviceName){
        int index = oldEndpoint.indexOf(serviceName);

        String strippedRoute = oldEndpoint.substring(index + serviceName.length());
        System.out.println("Target route: " + String.format("%s/%s", newEndpoint, strippedRoute));
        return String.format("%s/%s", newEndpoint, strippedRoute);
    }

    private String getVerb(HttpServletRequest request) {
        String sMethod = request.getMethod();
        return sMethod.toUpperCase();
    }

    private HttpHost getHttpHost(URL host) {
        HttpHost httpHost = new HttpHost(host.getHost(), host.getPort(),
                host.getProtocol());
        return httpHost;
    }

    private Header[] convertHeaders(MultiValueMap<String, String> headers) {
        List<Header> list = new ArrayList<>();
        for (String name : headers.keySet()) {
            for (String value : headers.get(name)) {
                list.add(new BasicHeader(name, value));
            }
        }
        return list.toArray(new BasicHeader[0]);
    }

    private HttpResponse forwardRequest(HttpClient httpclient, HttpHost httpHost,
                                        HttpRequest httpRequest) throws IOException {
        return httpclient.execute(httpHost, httpRequest);
    }


    private MultiValueMap<String, String> revertHeaders(Header[] headers) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        for (Header header : headers) {
            String name = header.getName();
            if (!map.containsKey(name)) {
                map.put(name, new ArrayList<String>());
            }
            map.get(name).add(header.getValue());
        }
        return map;
    }

    private InputStream getRequestBody(HttpServletRequest request) {
        InputStream requestEntity = null;
        try {
            requestEntity = request.getInputStream();
        }
        catch (IOException ex) {
            // no requestBody is ok.
        }
        return requestEntity;
    }

    private void setResponse(HttpResponse response) throws IOException {
        this.helper.setResponse(response.getStatusLine().getStatusCode(),
                response.getEntity() == null ? null : response.getEntity().getContent(),
                revertHeaders(response.getAllHeaders()));
    }

    private HttpResponse forward(HttpClient httpclient, String verb, String uri,
                                 HttpServletRequest request, MultiValueMap<String, String> headers,
                                 MultiValueMap<String, String> params, InputStream requestEntity)
            throws Exception {
        Map<String, Object> info = this.helper.debug(verb, uri, headers, params,
                requestEntity);
        URL host = new URL( uri );
        HttpHost httpHost = getHttpHost(host);

        HttpRequest httpRequest;
        int contentLength = request.getContentLength();
        InputStreamEntity entity = new InputStreamEntity(requestEntity, contentLength,
                request.getContentType() != null
                        ? ContentType.create(request.getContentType()) : null);
        switch (verb.toUpperCase()) {
            case "POST":
                HttpPost httpPost = new HttpPost(uri);
                httpRequest = httpPost;
                httpPost.setEntity(entity);
                break;
            case "PUT":
                HttpPut httpPut = new HttpPut(uri);
                httpRequest = httpPut;
                httpPut.setEntity(entity);
                break;
            case "PATCH":
                HttpPatch httpPatch = new HttpPatch(uri );
                httpRequest = httpPatch;
                httpPatch.setEntity(entity);
                break;
            default:
                httpRequest = new BasicHttpRequest(verb, uri);

        }
        try {
            httpRequest.setHeaders(convertHeaders(headers));
            HttpResponse zuulResponse = forwardRequest(httpclient, httpHost, httpRequest);

            return zuulResponse;
        }
        finally {
        }
    }



    public boolean useSpecialRoute(AbTestingRoute testRoute){
        Random random = new Random();

        if (testRoute.getActive().equals("N")) {
            logger.debug("SpecialRoutesFilter.useSpecialRoute() - route not active");
            return false;
        }

        int value = random.nextInt((10 - 1) + 1) + 1;
        logger.debug("SpecialRoutesFilter.useSpecialRoute() - Random Value: {}, Weight: {}", value, testRoute.getWeight());

        if (testRoute.getWeight()<value) {
            logger.debug("SpecialRoutesFilter.useSpecialRoute() - Sending the request to the new version of the service...");
            return true;
        }

        return false;
    }

    /**
     * The SpecialRoutes service will check an internal database to see if the service name exists. If the targeted service
     * name exists, it will return a weight and target destination of an alternative location for the service.
     * The SpecialRoutesFilter will then take the weight returned and, based on the weight, randomly generate a
     * number that will be used to determine whether the user’s call will be routed to the alternative organization service
     * or to the organization service defined in the Zuul route mappings.
     */
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();

        AbTestingRoute abTestRoute = getAbRoutingInfo( filterUtils.getServiceId() );
        logger.debug("SpecialRoutesFilter.run() - serviceId {}" , filterUtils.getServiceId());

        if (abTestRoute!=null && useSpecialRoute(abTestRoute)) {
            logger.debug("SpecialRoutesFilter.run() - useSpecialRoute: true, ctx.getRequest().getRequestURI(): {}, " +
                    " abTestRoute.getEndpoint(): {}, ctx.get(\"serviceId\"): {}", ctx.getRequest().getRequestURI(), abTestRoute.getEndpoint(), ctx.get("serviceId").toString());

            // build the full URL (with path) to the service location specified by the specialroutes service
            String route = buildRouteString(ctx.getRequest().getRequestURI(),
                    abTestRoute.getEndpoint(),
                    ctx.get("serviceId").toString());
            logger.debug("SpecialRoutesFilter.run() - forwardToSpecialRoute: {}", route);
            forwardToSpecialRoute(route); // does the work of forwarding onto the alternative service
        }
        logger.debug("SpecialRoutesFilter.run() - useSpecialRoute: false");
        return null;
    }

    // NOTE: The code in this method borrows heavily from the source code for the Spring Cloud SimpleHostRoutingFilter class!!!
    private void forwardToSpecialRoute(String route) {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();

        /**
         * Copying all of the values from the incoming HTTP request:
         *  - the header parameters,
         *  - HTTP verb,
         *  - the body
         *  into a new request that will  be invoked on the target service
         */
        MultiValueMap<String, String> headers = this.helper
                .buildZuulRequestHeaders(request);
        MultiValueMap<String, String> params = this.helper
                .buildZuulRequestQueryParams(request);
        String verb = getVerb(request);
        // Makes a copy of the HTTP Body that will be forwarded onto the alternative service
        InputStream requestEntity = getRequestBody(request);
        if (request.getContentLength() < 0) {
            context.setChunkedRequestBody();
        }

        this.helper.addIgnoredHeaders();
        CloseableHttpClient httpClient = null;
        HttpResponse response = null;

        try {
            httpClient  = HttpClients.createDefault();
            // Invokes the alternative service using the forward helper method
            response = forward(httpClient, verb, route, request, headers,
                    params, requestEntity);
            // The forwardToSpecialRoute() method takes the response back from the target service and sets it on the
            // HTTP request context used by Zuul.
            // Zuul uses the HTTP request context to return the response back from the calling service client.
            setResponse(response);
        }
        catch (Exception ex ) {
            ex.printStackTrace();

        }
        finally{
            try {
                httpClient.close();
            }
            catch(IOException ex){}
        }
    }
}
