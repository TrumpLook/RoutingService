package com.example.demo.controllers;

import com.example.demo.dto.ActiveTrafficEventResponse;
import com.example.demo.services.TrafficEventService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/events")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class TrafficEventController {
    private final TrafficEventService trafficEventService;

    public TrafficEventController(TrafficEventService trafficEventService) {
        this.trafficEventService = trafficEventService;
    }

    @GetMapping("/active")
    public List<ActiveTrafficEventResponse> getActiveEvents() {
        return trafficEventService.getActiveEvents();
    }
}
