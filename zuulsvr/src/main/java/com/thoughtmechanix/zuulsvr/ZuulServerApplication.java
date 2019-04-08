package com.thoughtmechanix.zuulsvr;


import com.thoughtmechanix.zuulsvr.utils.UserContextInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * This dependency tells the Spring Cloud framework that this service will be running Zuul and initialize Zuul appropriately
 */
@SpringBootApplication
@EnableZuulProxy
public class ZuulServerApplication {

    //Zuul will automatically use Eureka to look up services by their service IDs and then use
    //Netflix Ribbon to do client-side load balancing of requests from within Zuul
    @LoadBalanced
    @Bean
    public RestTemplate getRestTemplate(){
        RestTemplate template = new RestTemplate();
        List interceptors = template.getInterceptors();
        if (interceptors == null) {
            template.setInterceptors(Collections.singletonList(new UserContextInterceptor()));
        } else {
            interceptors.add(new UserContextInterceptor());
            template.setInterceptors(interceptors);
        }

        return template;
    }

    public static void main(String[] args) {
        SpringApplication.run(ZuulServerApplication.class, args);
    }
}

