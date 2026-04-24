package com.example.demo.dto;

import java.util.List;

public record RouteResponse(
        boolean pathFound,
        double totalWeight,
        int visitedNodes,
        int relaxedEdges,
        long executionTimeNanos,
        List<RoutePoint> points
) {
    public record RoutePoint(
            double lat,
            double lon
    ) {
    }
}
