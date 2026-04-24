package com.example.demo.dto;

public record TrafficEventMessage(
        String eventId,
        String eventType,
        String action,
        double lat,
        double lon,
        double radiusMeters,
        String createdAt
) {
}
