package com.thoughtmechanix.zuulsvr.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * All Zuul filters must extend the ZuulFilter class and override four methods:
        - filterType()
        - filterOrder()
        - shouldFilter()
        - run()
 * This pre-filter inspects all incoming requests to the gateway and determine whether there’s an HTTP header
 * called tmx-correlationid present in the request
 */
@Component
public class TrackingFilter extends ZuulFilter{
    private static final int      FILTER_ORDER =  1;
    private static final boolean  SHOULD_FILTER=true;
    private static final Logger logger = LoggerFactory.getLogger(TrackingFilter.class);

    @Autowired
    FilterUtils filterUtils;

    @Override
    public String filterType() {
        return FilterUtils.PRE_FILTER_TYPE;
    }

    //The filterOrder() method returns an integer value indicating what order Zuul should send requests through the different filter types.
    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }

    //The shouldFilter() method returns a Boolean indicating whether or not the filter should be active.
    @Override
    public boolean shouldFilter() {
        return SHOULD_FILTER;
    }

    private boolean isCorrelationIdPresent(){
      if (filterUtils.getCorrelationId() !=null){
          return true;
      }

      return false;
    }

    private String generateCorrelationId(){
        return java.util.UUID.randomUUID().toString();
    }

    //The run() method is the code that is executed every time a service passes through the filter. In your run() function,
    //you check to see if the tmxcorrelationid is present and if it isn’t, you generate a correlation value and set the
    //tmx-correlation-id HTTP
    public Object run() {

        //The presence of a correlationID means that this particular service call is part of a chain of service calls
        // carrying out the user’s request.
        if (isCorrelationIdPresent()) {
           logger.debug("2 - TrackingFilter.run() - tmx-correlation-id found in tracking filter: {}", filterUtils.getCorrelationId());
        }
        else{
            filterUtils.setCorrelationId(generateCorrelationId());
            logger.debug("2 - TrackingFilter.run() - tmx-correlation-id generated in tracking filter: {}", filterUtils.getCorrelationId());
        }

        RequestContext ctx = RequestContext.getCurrentContext();
        logger.debug("3 - TrackingFilter.run() - Processing incoming request for {}",  ctx.getRequest().getRequestURI());
        return null;
    }
}