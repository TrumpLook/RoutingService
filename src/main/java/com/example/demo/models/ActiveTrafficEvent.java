package com.example.demo.models;

import java.util.List;

public record ActiveTrafficEvent(
        String eventId,
        String eventType,
        String action,
        double lat,
        double lon,
        double radiusMeters,
        String createdAt,
        long nodeId,
        List<BlockedEdgeRef> blockedEdges
) {
}
