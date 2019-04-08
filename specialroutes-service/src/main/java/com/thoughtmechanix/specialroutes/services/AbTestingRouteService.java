package com.thoughtmechanix.specialroutes.services;


import com.thoughtmechanix.specialroutes.controllers.SpecialRoutesServiceController;
import com.thoughtmechanix.specialroutes.exception.NoRouteFound;
import com.thoughtmechanix.specialroutes.model.AbTestingRoute;
import com.thoughtmechanix.specialroutes.repository.AbTestingRouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AbTestingRouteService {

    private static final Logger logger = LoggerFactory.getLogger(SpecialRoutesServiceController.class);

    @Autowired
    private AbTestingRouteRepository abTestingRouteRepository;

    public AbTestingRoute getRoute(String serviceName) {
        // The SpecialRoutes service will check an internal database to see if the service name exists. If the targeted service
        //name exists, it will return a weight and target destination of an alternative location for the service.
        AbTestingRoute route = abTestingRouteRepository.findByServiceName(serviceName);

        if (route==null){
            throw new NoRouteFound();
        } else {
            logger.debug("AbTestingRouteService Hit -> route from DB: {}" , route.toString());
        }

        return route;
    }

    public void saveAbTestingRoute(AbTestingRoute route){

        abTestingRouteRepository.save(route);

    }

    public void updateRouteAbTestingRoute(AbTestingRoute route){
        abTestingRouteRepository.save(route);
    }

    public void deleteRoute(AbTestingRoute route){
        abTestingRouteRepository.delete(route.getServiceName());
    }
}
