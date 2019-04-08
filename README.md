# Introduction
Welcome to Spring Microservices in Action, Chapter 6.  Chapter 6 introduces the concept of a API gateway. API gateways are using to enforce consistent policies and actions on all service calls.  With this chapter we are going to introduce Spring Cloud and Netflix Zuul API gateway.  

By the time you are done reading this chapter you will have built and/or deployed:

1.  A Spring Cloud Config server that is deployed as Docker container and can manage a services configuration information using a file system or GitHub-based repository.
2.  A Eureka server running as a Spring-Cloud based service.  This service will allow multiple service instances to register with it.  Clients that need to call a service will use Eureka to lookup the physical location of the target service.
3.  A Zuul API Gateway.  All of our microservices can be routed through the gateway and have pre, response and
post policies enforced on the calls.
4.  A organization service that will manage organization data used within EagleEye.
5.  A new version of the organization service.  This service is used to demonstrate how to use the Zuul API gateway to route to different versions of a service.
6.  A special routes services that the the API gateway will call to determine whether or not it should route a user's service call to a different service then the one they were originally calling.  This service is used in conjunction with the orgservice-new service to determine whether a call to the organization service gets routed to an old version of the organization service vs. a new version of the service.
7.  A licensing service that will manage licensing data used within EagleEye.
8.  A Postgres SQL database used to hold the data for these two services.

# Software needed
1.	[Apache Maven] (http://maven.apache.org). I used version 3.3.9 of the Maven. I chose Maven because, while other build tools like Gradle are extremely popular, Maven is still the pre-dominate build tool in use in the Java ecosystem. All of the code examples in this book have been compiled with Java version 1.8.
2.	[Docker] (http://docker.com). I built the code examples in this book using Docker V1.12 and above. I am taking advantage of the embedded DNS server in Docker that came out in release V1.11. New Docker releases are constantly coming out so it's release version you are using may change on a regular basis.
3.	[Git Client] (http://git-scm.com). All of the source code for this book is stored in a GitHub repository. For the book, I used version 2.8.4 of the git client.

# Building the Docker Images for Chapter 6
To build the code examples for Chapter 6 as a docker image, open a command-line window change to the directory where you have downloaded the chapter 6 source code.

Run the following maven command.  This command will execute the [Spotify docker plugin](https://github.com/spotify/docker-maven-plugin) defined in the pom.xml file.  

   **mvn clean package docker:build**

 Running the above command at the root of the project directory will build all of the projects.  If everything builds successfully you should see a message indicating that the build was successful.

# Running the services in Chapter 6

Now we are going to use docker-compose to start the actual image.  To start the docker image,
change to the directory containing  your chapter 6 source code.  Issue the following docker-compose command:

   **docker-compose -f docker/common/docker-compose.yml up**

If everything starts correctly you should see a bunch of Spring Boot information fly by on standard out.  At this point all of the services needed for the chapter code examples will be running.

# LOGGING SAMPLES

   **Simple_Log_Trace -  Using licensingservice to getOrganization info:**
    
zuulserver_1             | 2019-04-08 17:25:14.851 : 1 - UserContextFilter.doFilter() - Special Routes Service Incoming Correlation id: null
zuulserver_1             | 2019-04-08 17:25:14.854 : 2 - TrackingFilter.run() - tmx-correlation-id generated in tracking filter: bdf0ff74-4303-4788-8a83-b15a5728d388
zuulserver_1             | 2019-04-08 17:25:14.854 : 3 - TrackingFilter.run() - Processing incoming request for /api/licensing/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a/licenses/getOrganization

specialroutes-service_1  | 2019-04-08 17:25:14.928 : specialroutes.UserContextFilter.doFilter() - Special Routes Service Incoming Correlation id:
specialroutes-service_1  | 2019-04-08 17:25:15.067 : SpecialRoutesServiceController Hit - Invoked by zuulsvr.SpecialRoutesFilter.getAbRoutingInfo() -> v1/route/abtesting/{serviceName} serviceName: licensingservice
specialroutes-service_1  | Hibernate: select abtestingr0_.service_name as service_1_0_, abtestingr0_.active as active2_0_, abtestingr0_.endpoint as endpoint3_0_, abtestingr0_.weight as weight4_0_ from abtesting abtestingr0_ where abtestingr0_.service_name=?

zuulserver_1             | 2019-04-08 17:25:15.314 : SpecialRoutesFilter.run() - serviceId licensingservice
zuulserver_1             | 2019-04-08 17:25:15.315 : SpecialRoutesFilter.run() - useSpecialRoute: false

licensingservice_1       | 2019-04-08 17:25:15.372 : from zuulsvr.TrackingFilter -> licenses.doFilter() - License Service Incoming Correlation id: bdf0ff74-4303-4788-8a83-b15a5728d388
licensingservice_1       | 2019-04-08 17:25:15.413 : LicenseServiceController -> /getOrganization - Found tmx-correlation-id in license-service-controller: bdf0ff74-4303-4788-8a83-b15a5728d388
licensingservice_1       | 2019-04-08 17:25:15.426 : LicenseService.getOrganization() - getting Organization info: e254f8c-c442-4ebe-a82a-e2fc1d1ff78a
licensingservice_1       | 2019-04-08 17:25:15.427 : >>> In Licensing Service.getOrganization - rest exchange to OrganizationService: bdf0ff74-4303-4788-8a83-b15a5728d388 - Thread Id: 49
licensingservice_1       | 2019-04-08 17:25:15.450 : licenses.UserContextInterceptor - License Service Incoming Correlation id: d9381aaa-5be1-40ee-8ca5-e725a43f0ad5


zuulserver_1             | 2019-04-08 17:25:15.468 : 1 - UserContextFilter.doFilter() - Special Routes Service Incoming Correlation id: bdf0ff74-4303-4788-8a83-b15a5728d388
zuulserver_1             | 2019-04-08 17:25:15.473 : 2 - TrackingFilter.run() - tmx-correlation-id found in tracking filter: bdf0ff74-4303-4788-8a83-b15a5728d388
zuulserver_1             | 2019-04-08 17:25:15.473 : 3 - TrackingFilter.run() - Processing incoming request for /api/organization/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a

specialroutes-service_1  | 2019-04-08 17:25:15.496 : specialroutes.UserContextFilter.doFilter() - Special Routes Service Incoming Correlation id: bdf0ff74-4303-4788-8a83-b15a5728d388
specialroutes-service_1  | 2019-04-08 17:25:15.518 : SpecialRoutesServiceController Hit - Invoked by zuulsvr.SpecialRoutesFilter.getAbRoutingInfo() -> v1/route/abtesting/{serviceName} serviceName: organizationservice
specialroutes-service_1  | Hibernate: select abtestingr0_.service_name as service_1_0_, abtestingr0_.active as active2_0_, abtestingr0_.endpoint as endpoint3_0_, abtestingr0_.weight as weight4_0_ from abtesting abtestingr0_ where abtestingr0_.service_name=?
specialroutes-service_1  | 2019-04-08 17:25:15.542 : AbTestingRouteService Hit -> route from DB: AbTestingRoute{serviceName='organizationservice', active='Y', endpoint='http://orgservice-new:8087', weight=5}

zuulserver_1             | 2019-04-08 17:25:15.554 : SpecialRoutesFilter.run() - serviceId organizationservice
zuulserver_1             | 2019-04-08 17:25:15.555 : SpecialRoutesFilter.run() - useSpecialRoute: false

organizationservice_1    | 2019-04-08 17:25:15.585 : from zuulsvr.TrackingFilter -> organization.doFilter() - Organization Service Incoming Correlation id: bdf0ff74-4303-4788-8a83-b15a5728d388
organizationservice_1    | 2019-04-08 17:25:15.638 : OrganizationServiceController Hit -> {organizationId}/ - getOrg e254f8c-c442-4ebe-a82a-e2fc1d1ff78a
organizationservice_1    | Hibernate: select organizati0_.organization_id as organiza1_0_, organizati0_.contact_email as contact_2_0_, organizati0_.contact_name as contact_3_0_, organizati0_.contact_phone as contact_4_0_, organizati0_.name as name5_0_ from organizations organizati0_ where organizati0_.organization_id=?

zuulserver_1             | 2019-04-08 17:25:15.683 : 4 - ResponseFilter.run() - Adding the correlation id to the outbound headers. bdf0ff74-4303-4788-8a83-b15a5728d388
zuulserver_1             | 2019-04-08 17:25:15.684 : 5 - ResponseFilter.run() - Completing outgoing request for /api/organization/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a
zuulserver_1             | 2019-04-08 17:25:15.684 : ----------------------------------------------------------------------------------------------------------------------
zuulserver_1             | 2019-04-08 17:25:15.776 : 4 - ResponseFilter.run() - Adding the correlation id to the outbound headers. bdf0ff74-4303-4788-8a83-b15a5728d388
zuulserver_1             | 2019-04-08 17:25:15.776 : 5 - ResponseFilter.run() - Completing outgoing request for /api/licensing/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a/licenses/getOrganization

   **Log_Trace_orgservice_old_USED:**

zuulserver_1             | 2019-04-05 19:09:32.707 : 1 - UserContextFilter.doFilter() - Special Routes Service Incoming Correlation id: null
zuulserver_1             | 2019-04-05 19:09:32.721 : 2 - TrackingFilter.run() - tmx-correlation-id generated in tracking filter: 4449c8d7-a290-48cd-b7b4-6bece8667dae
zuulserver_1             | 2019-04-05 19:09:32.722 : 3 - TrackingFilter.run() - Processing incoming request for /api/licensing/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a/licenses/f3831f8c-c338-4ebe-a82a-e2fc1d1ff78a

specialroutes-service_1  | 2019-04-05 19:09:32.776 : specialroutes.UserContextFilter.doFilter() - Special Routes Service Incoming Correlation id:
specialroutes-service_1  | Hibernate: select abtestingr0_.service_name as service_1_0_, abtestingr0_.active as active2_0_, abtestingr0_.endpoint as endpoint3_0_, abtestingr0_.weight as weight4_0_ from abtesting abtestingr0_ where abtestingr0_.service_name=?

licensingservice_1       | 2019-04-05 19:09:32.917 : from zuulsvr.TrackingFilter -> licenses.doFilter() - License Service Incoming Correlation id: 4449c8d7-a290-48cd-b7b4-6bece8667dae
licensingservice_1       | 2019-04-05 19:09:32.946 : LicenseServiceController -> licenses/{licenseId} - Found tmx-correlation-id in license-service-controller: 4449c8d7-a290-48cd-b7b4-6bece8667dae
licensingservice_1       | Hibernate: select license0_.license_id as license_1_0_, license0_.comment as comment2_0_, license0_.license_allocated as license_3_0_, license0_.license_max as license_4_0_, license0_.license_type as license_5_0_, license0_.organization_id as organiza6_0_, license0_.product_name as product_7_0_ from licenses license0_ where license0_.organization_id=? and license0_.license_id=?
licensingservice_1       | 2019-04-05 19:09:32.994 : LicenseService.getLicense() - getting Organization info: e254f8c-c442-4ebe-a82a-e2fc1d1ff78a

zuulserver_1             | 2019-04-05 19:09:33.014 : 1 - UserContextFilter.doFilter() - Special Routes Service Incoming Correlation id: 4449c8d7-a290-48cd-b7b4-6bece8667dae
zuulserver_1             | 2019-04-05 19:09:33.016 : 2 - TrackingFilter.run() - tmx-correlation-id found in tracking filter: 4449c8d7-a290-48cd-b7b4-6bece8667dae
zuulserver_1             | 2019-04-05 19:09:33.016 : 3 - TrackingFilter.run() - Processing incoming request for /api/organization/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a

specialroutes-service_1  | 2019-04-05 19:09:33.028 : specialroutes.UserContextFilter.doFilter() - Special Routes Service Incoming Correlation id: 4449c8d7-a290-48cd-b7b4-6bece8667dae
specialroutes-service_1  | Hibernate: select abtestingr0_.service_name as service_1_0_, abtestingr0_.active as active2_0_, abtestingr0_.endpoint as endpoint3_0_, abtestingr0_.weight as weight4_0_ from abtesting abtestingr0_ where abtestingr0_.service_name=?

orgservice-new_1         | 2019-04-05 19:09:33.155 : Organization Service (NEW) Incoming Correlation id: 4449c8d7-a290-48cd-b7b4-6bece8667dae
orgservice-new_1         | 2019-04-05 19:09:33.197 : Looking up data for org e254f8c-c442-4ebe-a82a-e2fc1d1ff78a
orgservice-new_1         | Hibernate: select organizati0_.organization_id as organiza1_0_, organizati0_.contact_email as contact_2_0_, organizati0_.contact_name as contact_3_0_, organizati0_.contact_phone as contact_4_0_, organizati0_.name as name5_0_ from organizations organizati0_ where organizati0_.organization_id=?

organizationservice_1    | 2019-04-05 19:09:33.279 : from zuulsvr.TrackingFilter -> organization.doFilter() - Organization Service Incoming Correlation id: 4449c8d7-a290-48cd-b7b4-6bece8667dae
organizationservice_1    | 2019-04-05 19:09:33.352 : OrganizationServiceController Hit -> {organizationId}/ - getOrg e254f8c-c442-4ebe-a82a-e2fc1d1ff78a
organizationservice_1    | Hibernate: select organizati0_.organization_id as organiza1_0_, organizati0_.contact_email as contact_2_0_, organizati0_.contact_name as contact_3_0_, organizati0_.contact_phone as contact_4_0_, organizati0_.name as name5_0_ from organizations organizati0_ where organizati0_.organization_id=?

zuulserver_1             | Target route: http://orgservice-new:8087/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a
zuulserver_1             | 2019-04-05 19:09:33.452 : 4 - ResponseFilter.run() - Adding the correlation id to the outbound headers. 4449c8d7-a290-48cd-b7b4-6bece8667dae
zuulserver_1             | 2019-04-05 19:09:33.452 : 5 - ResponseFilter.run() - Completing outgoing request for /api/organization/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a
zuulserver_1             | 2019-04-05 19:09:33.452 : ----------------------------------------------------------------------------------------------------------------------
zuulserver_1             | 2019-04-05 19:09:33.493 : 4 - ResponseFilter.run() - Adding the correlation id to the outbound headers. 4449c8d7-a290-48cd-b7b4-6bece8667dae
zuulserver_1             | 2019-04-05 19:09:33.495 : 5 - ResponseFilter.run() - Completing outgoing request for /api/licensing/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a/licenses/f3831f8c-c338-4ebe-a82a-e2fc1d1ff78a

   **Log_Trace_orgservice_new_USED````:**
   
zuulserver_1             | 2019-04-05 22:27:08.291 : 1 - UserContextFilter.doFilter() - Special Routes Service Incoming Correlation id: null
zuulserver_1             | 2019-04-05 22:27:08.293 : 2 - TrackingFilter.run() - tmx-correlation-id generated in tracking filter: 7e66b7e6-5633-4a57-bb1e-3f093f52ef6a
zuulserver_1             | 2019-04-05 22:27:08.295 : 3 - TrackingFilter.run() - Processing incoming request for /api/licensing/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a/licenses/f3831f8c-c338-4ebe-a82a-e2fc1d1ff78a

specialroutes-service_1  | 2019-04-05 22:27:08.307 : specialroutes.UserContextFilter.doFilter() - Special Routes Service Incoming Correlation id:
specialroutes-service_1  | 2019-04-05 22:27:08.312 : SpecialRoutesServiceController Hit - Invoked by zuulsvr.SpecialRoutesFilter.getAbRoutingInfo() -> v1/route/abtesting/{serviceName} serviceName: licensingservice
zuulserver_1             | 2019-04-05 22:27:08.331 : SpecialRoutesFilter.run() - serviceId licensingservice
zuulserver_1             | 2019-04-05 22:27:08.331 : SpecialRoutesFilter.run() - useSpecialRoute: false

licensingservice_1       | 2019-04-05 22:27:08.344 : from zuulsvr.TrackingFilter -> licenses.doFilter() - License Service Incoming Correlation id: 7e66b7e6-5633-4a57-bb1e-3f093f52ef6a
licensingservice_1       | 2019-04-05 22:27:08.353 : LicenseServiceController -> licenses/{licenseId} - Found tmx-correlation-id in license-service-controller: 7e66b7e6-5633-4a57-bb1e-3f093f52ef6a
licensingservice_1       | 2019-04-05 22:27:08.364 : LicenseService.getLicense() - getting Organization info: e254f8c-c442-4ebe-a82a-e2fc1d1ff78a
licensingservice_1       | Hibernate: select license0_.license_id as license_1_0_, license0_.comment as comment2_0_, license0_.license_allocated as license_3_0_, license0_.license_max as license_4_0_, license0_.license_type as license_5_0_, license0_.organization_id as organiza6_0_, license0_.product_name as product_7_0_ from licenses license0_ where license0_.organization_id=? and license0_.license_id=?
licensingservice_1       | 2019-04-05 22:27:08.366 : >>> In Licensing Service.getOrganization - rest exchange to OrganizationService: 7e66b7e6-5633-4a57-bb1e-3f093f52ef6a - Thread Id: 75

zuulserver_1             | 2019-04-05 22:27:08.372 : 1 - UserContextFilter.doFilter() - Special Routes Service Incoming Correlation id: 7e66b7e6-5633-4a57-bb1e-3f093f52ef6a
zuulserver_1             | 2019-04-05 22:27:08.373 : 2 - TrackingFilter.run() - tmx-correlation-id found in tracking filter: 7e66b7e6-5633-4a57-bb1e-3f093f52ef6a
zuulserver_1             | 2019-04-05 22:27:08.375 : 3 - TrackingFilter.run() - Processing incoming request for /api/organization/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a
specialroutes-service_1  | Hibernate: select abtestingr0_.service_name as service_1_0_, abtestingr0_.active as active2_0_, abtestingr0_.endpoint as endpoint3_0_, abtestingr0_.weight as weight4_0_ from abtesting abtestingr0_ where abtestingr0_.service_name=?

specialroutes-service_1  | 2019-04-05 22:27:08.399 : specialroutes.UserContextFilter.doFilter() - Special Routes Service Incoming Correlation id: 7e66b7e6-5633-4a57-bb1e-3f093f52ef6a
specialroutes-service_1  | 2019-04-05 22:27:08.400 : SpecialRoutesServiceController Hit -> v1/route/abtesting/{serviceName} serviceName: organizationservice
specialroutes-service_1  | Hibernate: select abtestingr0_.service_name as service_1_0_, abtestingr0_.active as active2_0_, abtestingr0_.endpoint as endpoint3_0_, abtestingr0_.weight as weight4_0_ from abtesting abtestingr0_ where abtestingr0_.service_name=?
specialroutes-service_1  | 2019-04-05 22:27:08.404 : AbTestingRouteService Hit -> route from DB: AbTestingRoute{serviceName='organizationservice', active='Y', endpoint='http://orgservice-new:8087', weight=5}

zuulserver_1             | 2019-04-05 22:27:08.408 : SpecialRoutesFilter.run() - serviceId organizationservice
zuulserver_1             | 2019-04-05 22:27:08.409 : SpecialRoutesFilter.run() - useSpecialRoute: true, ctx.getRequest().getRequestURI(): /api/organization/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a,  		abTestRoute.getEndpoint(): http://orgservice-new:8087, ctx.get("serviceId"): organizationservice
zuulserver_1             | Target route: http://orgservice-new:8087/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a
zuulserver_1             | 2019-04-05 22:27:08.409 : SpecialRoutesFilter.run() - forwardToSpecialRoute: http://orgservice-new:8087/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a
orgservice-new_1         | Hibernate: select organizati0_.organization_id as organiza1_0_, organizati0_.contact_email as contact_2_0_, organizati0_.contact_name as contact_3_0_, organizati0_.contact_phone as contact_4_0_, organizati0_.name as name5_0_ from organizations organizati0_ where organizati0_.organization_id=?

orgservice-new_1         | 2019-04-05 22:27:08.424 : Organization Service (NEW) Incoming Correlation id: 7e66b7e6-5633-4a57-bb1e-3f093f52ef6a
orgservice-new_1         | 2019-04-05 22:27:08.430 : Looking up data for org e254f8c-c442-4ebe-a82a-e2fc1d1ff78a
orgservice-new_1         | Hibernate: select organizati0_.organization_id as organiza1_0_, organizati0_.contact_email as contact_2_0_, organizati0_.contact_name as contact_3_0_, organizati0_.contact_phone as contact_4_0_, organizati0_.name as name5_0_ from organizations organizati0_ where organizati0_.organization_id=?
zuulserver_1             | 2019-04-05 22:27:08.443 : SpecialRoutesFilter.run() - useSpecialRoute: false

orgservice-new_1         | 2019-04-05 22:27:08.455 : Organization Service (NEW) Incoming Correlation id: 7e66b7e6-5633-4a57-bb1e-3f093f52ef6a
orgservice-new_1         | 2019-04-05 22:27:08.467 : Looking up data for org e254f8c-c442-4ebe-a82a-e2fc1d1ff78a
zuulserver_1             | 2019-04-05 22:27:08.482 : 4 - ResponseFilter.run() - Adding the correlation id to the outbound headers. 7e66b7e6-5633-4a57-bb1e-3f093f52ef6a
zuulserver_1             | 2019-04-05 22:27:08.485 : 5 - ResponseFilter.run() - Completing outgoing request for /api/organization/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a
zuulserver_1             | 2019-04-05 22:27:08.485 : ----------------------------------------------------------------------------------------------------------------------
zuulserver_1             | 2019-04-05 22:27:08.521 : 4 - ResponseFilter.run() - Adding the correlation id to the outbound headers. 7e66b7e6-5633-4a57-bb1e-3f093f52ef6a
zuulserver_1             | 2019-04-05 22:27:08.524 : 5 - ResponseFilter.run() - Completing outgoing request for /api/licensing/v1/organizations/e254f8c-c442-4ebe-a82a-e2fc1d1ff78a/licenses/f3831f8c-c338-4ebe-a82a-e2fc1d1ff78a