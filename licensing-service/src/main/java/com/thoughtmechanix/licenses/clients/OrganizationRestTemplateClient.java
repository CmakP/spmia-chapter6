package com.thoughtmechanix.licenses.clients;

import com.thoughtmechanix.licenses.model.Organization;
import com.thoughtmechanix.licenses.utils.UserContext;
import com.thoughtmechanix.licenses.utils.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * The licensing service business logic needs to execute a call to the organization service.
 * A RestTemplate is used to invoke the organization service. The RestTemplate will use a
 * custom Spring Interceptor class (UserContextInterceptor) to inject the correlation ID into
 * the outbound call as an HTTP header.
 */
@Component
public class OrganizationRestTemplateClient {

    @Autowired // With the bean definition in place, any time you use this annotation and inject a RestTemplate into a class,
    // you’ll use the RestTemplate created in the bean with the UserContextInterceptor attached to it.
    RestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(OrganizationRestTemplateClient.class);

    public Organization getOrganization(String organizationId){
        logger.debug(">>> In Licensing Service.getOrganization - rest exchange to OrganizationService: {} - Thread Id: {}", UserContextHolder.getContext().getCorrelationId(), Thread.currentThread().getId());
        //NOTE: Before the actual HTTP service call occurs by the RestTemplate, UserContextInterceptor is invoked
        ResponseEntity<Organization> restExchange =
                restTemplate.exchange(
                        "http://zuulservice/api/organization/v1/organizations/{organizationId}",
                        HttpMethod.GET,
                        null, Organization.class, organizationId);
        return restExchange.getBody();
    }
}
