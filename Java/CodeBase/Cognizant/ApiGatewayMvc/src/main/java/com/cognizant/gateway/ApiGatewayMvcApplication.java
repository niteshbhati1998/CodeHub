package com.cognizant.gateway;

import static org.springframework.cloud.gateway.server.mvc.filter.AfterFilterFunctions.dedupeResponseHeader;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.requestSize;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.method;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@RestController
@SpringBootApplication
public class ApiGatewayMvcApplication extends SpringBootServletInitializer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApiGatewayMvcApplication.class);
	//private final URI TARGET_URL_JAVA_SERVICE = URI.create("http://localhost:7777");
	
	public int stripPrefixParts = 1;

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayMvcApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ApiGatewayMvcApplication.class);
	}
	
	/**
	 * Creating A Route For Java AuthenticationService (/authentication/**)
	 * @return
	 */
	@CrossOrigin
	@Bean
	RouterFunction<ServerResponse> getRouteJavaAuthenticationService() {
		LOGGER.info("basic_route_java .......................");
		return route("basic_route_java")
				.before(stripPrefix(stripPrefixParts))
				.route(path("/authentication/**").and(method(POST, PUT, GET)), 
						http("http://localhost:7004"))
				.before(requestSize("5000MB"))  
				.after(dedupeResponseHeader("Access-Control-Allow-Credentials Access-Control-Allow-Origin"))
				.after(this::logResponseStatus)
				.onError(Throwable.class, this::handleException).build();
	}
	
	/**
	 * Creating A Route For Java LineageService (/lineage/**)
	 * @return
	 */
	@CrossOrigin
	@Bean
	RouterFunction<ServerResponse> getRouteJavaLineageService() {
		LOGGER.info("basic_route_java .......................");
		return route("basic_route_java")
				.before(stripPrefix(stripPrefixParts))
				.route(path("/lineage/**").and(method(POST, PUT, GET)), 
						http("http://localhost:7001"))
				.before(requestSize("5000MB")) 
				.after(dedupeResponseHeader("Access-Control-Allow-Credentials Access-Control-Allow-Origin"))
				.after(this::logResponseStatus)
				.onError(Throwable.class, this::handleException).build();
	}
	
	/**
	 * Creating A Route For Java AssessmentService (/assessment/**)
	 * @return
	 */
	@CrossOrigin
	@Bean
	RouterFunction<ServerResponse> getRouteJavaAssessmentService() {
		LOGGER.info("basic_route_java .......................");
		return route("basic_route_java")
				.before(stripPrefix(stripPrefixParts))
				.route(path("/assessment/**").and(method(POST, PUT, GET)), 
						http("http://localhost:7215"))
				.before(requestSize("5000MB")) 
				.after(dedupeResponseHeader("Access-Control-Allow-Credentials Access-Control-Allow-Origin"))
				.after(this::logResponseStatus)
				.onError(Throwable.class, this::handleException).build();
	}
	
	/**
	 * Creating A Route For Java VaultService (/service/vault/**)
	 * @return
	 */
	@CrossOrigin
	@Bean
	RouterFunction<ServerResponse> getRouteJavaVaultService() {
		LOGGER.info("basic_route_java .......................");
		return route("basic_route_java")
				.before(stripPrefix(stripPrefixParts))
				.route(path("/service/vault/**").and(method(POST, PUT, GET)), 
						http("http://localhost:7113"))
				.before(requestSize("5000MB")) 
				.after(dedupeResponseHeader("Access-Control-Allow-Credentials Access-Control-Allow-Origin"))
				.after(this::logResponseStatus)
				.onError(Throwable.class, this::handleException).build();
	}
	
	/**
	 * Creating A Route For Java CognosService (/cognosFileParsing/**,/getCognosexecutionsteps)
	 * @return
	 */
	@CrossOrigin
	@Bean
	RouterFunction<ServerResponse> getRouteJavaCognosService() {
		LOGGER.info("basic_route_java .......................");
		return route("basic_route_java")
				.before(stripPrefix(stripPrefixParts))
				.route(path("/cognosFileParsing/**").and(method(POST, PUT, GET)), 
						http("http://localhost:7011"))
				.route(path("/getCognosexecutionsteps").and(method(POST, PUT, GET)), 
						http("http://localhost:7011"))
				.before(requestSize("5000MB")) 
				.after(dedupeResponseHeader("Access-Control-Allow-Credentials Access-Control-Allow-Origin"))
				.after(this::logResponseStatus)
				.onError(Throwable.class, this::handleException).build();
	}
	
	/**
	 * It is used for testing GatewayService
	 * @param dataBean
	 * @return
	 */
	@CrossOrigin
	@GetMapping("/") 
	public ResponseEntity<?> checkConnection() {
		try {
			LOGGER.info("check gateway connection ");
			return new ResponseEntity<String>("gateway connection Success!", HttpStatus.OK);
		} catch (Exception e) {
			LOGGER.error("error in gateway connection", e);
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private ServerResponse handleException(Throwable throwable, ServerRequest request) {
		LOGGER.info("#handleException - failed to run request--uri " + request.uri());
		LOGGER.info("#handleException - failed to run request--message " + throwable.getMessage());
		ErrorDto errorDto = new ErrorDto();
		errorDto.setMessage(throwable.getMessage());
		errorDto.setDetails(throwable.getMessage());
		return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
	}

	private ServerResponse logResponseStatus(ServerRequest request, ServerResponse response) {
		HttpStatusCode httpStatusCode = response.statusCode();
		URI uri = request.uri();
		LOGGER.info("httpStatusCode: " + httpStatusCode + " For URI: " + uri);
		return response;
	}
}
