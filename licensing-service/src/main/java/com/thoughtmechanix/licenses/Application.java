package com.thoughtmechanix.licenses;

import com.thoughtmechanix.licenses.utils.UserContextInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
public class Application {

    /**
     * The licensing service business logic needs to execute a call to the organization service.
     * A RestTemplate is used to invoke the organization service. The RestTemplate will use a
     * custom Spring Interceptor class (UserContextInterceptor) to inject the correlation ID into
     * the outbound call as an HTTP header.
     *
     * With this bean definition in place, any time you use the @Autowired annotation and inject a RestTemplate
     * into a class, you’ll use the RestTemplate created here with UserContextInterceptor attached to it.
     */
    @LoadBalanced //The @LoadBalanced annotation indicates that this RestTemplate object is going to use Ribbon.
    @Bean // To use the UserContextInterceptor you need to define a RestTemplate bean and then add the UserContextInterceptor to it.
    public RestTemplate getRestTemplate(){
        RestTemplate template = new RestTemplate();
        List interceptors = template.getInterceptors();
        if (interceptors==null){ //Adding the UserContextInterceptor to the RestTemplate instance that has been created
            template.setInterceptors(Collections.singletonList(new UserContextInterceptor()));
        }
        else{
            interceptors.add(new UserContextInterceptor());
            template.setInterceptors(interceptors);
        }

        return template;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
