package com.example.demo.controllers;

import com.example.demo.dto.RouteRequest;
import com.example.demo.dto.RouteResponse;
import com.example.demo.services.RoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/route")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class RouteController {
    private static final Logger log = LoggerFactory.getLogger(RouteController.class);

    private final RoutingService routingService;

    public RouteController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @PostMapping
    public RouteResponse buildRoute(@RequestBody RouteRequest request) {
        return routingService.buildRoute(request);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(RuntimeException exception) {
        log.error("route error", exception);
        return Map.of("message", exception.getMessage());
    }
}
