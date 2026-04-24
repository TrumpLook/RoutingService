package com.example.demo.dto;

public record RouteRequest(
        double startLat,
        double startLon,
        double endLat,
        double endLon,
        String algorithm,
        String weightType
) {
}
