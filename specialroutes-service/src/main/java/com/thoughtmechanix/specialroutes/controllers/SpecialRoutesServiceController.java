package com.thoughtmechanix.specialroutes.controllers;



import com.thoughtmechanix.specialroutes.model.AbTestingRoute;
import com.thoughtmechanix.specialroutes.services.AbTestingRouteService;
import com.thoughtmechanix.specialroutes.utils.UserContextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(value="v1/route/")
public class SpecialRoutesServiceController {

    private static final Logger logger = LoggerFactory.getLogger(SpecialRoutesServiceController.class);

    @Autowired
    AbTestingRouteService routeService;

    @RequestMapping(value="abtesting/{serviceName}",method = RequestMethod.GET)
    public AbTestingRoute abstestings(@PathVariable("serviceName") String serviceName) {
        logger.debug("SpecialRoutesServiceController Hit - Invoked by zuulsvr.SpecialRoutesFilter.getAbRoutingInfo() -> v1/route/abtesting/{serviceName} serviceName: {}" , serviceName);

        return routeService.getRoute( serviceName);
    }

}
