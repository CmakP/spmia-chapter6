package com.thoughtmechanix.licenses.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * The UserContextInterceptor ensures that all outbound REST calls have the correlation ID from the UserContext in them.
 * The RestTemplate will use a custom Spring Interceptor class (UserContextInterceptor) to inject the correlation ID into
 * the outbound call as an HTTP header.
 *
 * This class is used to inject the correlation ID into any outgoing HTTP-based service requests being
 * EXECUTED FROM A RestTemplate INSTANCE.
 */
public class UserContextInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(UserContextInterceptor.class);

    //This is invoked before the actual HTTP service call occurs by the RestTemplate.
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        HttpHeaders headers = request.getHeaders();
        headers.add(UserContext.CORRELATION_ID, UserContextHolder.getContext().getCorrelationId());
        headers.add(UserContext.AUTH_TOKEN, UserContextHolder.getContext().getAuthToken());
        logger.debug("licenses.UserContextInterceptor - License Service Incoming Correlation id: {}", UserContextHolder.getContext().getCorrelationId());

        return execution.execute(request, body);
    }
}