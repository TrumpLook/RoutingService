package com.example.demo.services;

import com.example.demo.dto.ActiveTrafficEventResponse;
import com.example.demo.dto.TrafficEventMessage;
import com.example.demo.models.ActiveTrafficEvent;
import com.example.demo.models.BlockedEdgeRef;
import com.example.demo.models.RoadGraph;
import com.example.demo.models.RoadNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TrafficEventService {
    private static final Logger log = LoggerFactory.getLogger(TrafficEventService.class);

    private final GraphStorage graphStorage;
    private final GraphNodeRepository graphNodeRepository;
    private final ObjectMapper objectMapper;
    private final Map<String, ActiveTrafficEvent> activeEvents = new LinkedHashMap<>();

    public TrafficEventService(GraphStorage graphStorage,
                               GraphNodeRepository graphNodeRepository,
                               ObjectMapper objectMapper) {
        this.graphStorage = graphStorage;
        this.graphNodeRepository = graphNodeRepository;
        this.objectMapper = objectMapper;
    }

    public void handleKafkaMessage(String payload) {
        try {
            TrafficEventMessage event = objectMapper.readValue(payload, TrafficEventMessage.class);
            applyEvent(event);
            log.info("event ok");
        } catch (Exception exception) {
            log.error("event error", exception);
        }
    }

    public List<ActiveTrafficEventResponse> getActiveEvents() {
        List<ActiveTrafficEventResponse> response = new ArrayList<>(activeEvents.size());
        for (ActiveTrafficEvent activeEvent : activeEvents.values()) {
            response.add(new ActiveTrafficEventResponse(
                    activeEvent.eventId(),
                    activeEvent.eventType(),
                    activeEvent.action(),
                    activeEvent.lat(),
                    activeEvent.lon(),
                    activeEvent.radiusMeters(),
                    activeEvent.createdAt(),
                    activeEvent.nodeId()
            ));
        }
        return response;
    }

    private synchronized void applyEvent(TrafficEventMessage event) {
        String action = normalizeAction(event.action());
        String eventType = normalizeEventType(event.eventType());

        if ("TRAFFIC_JAM".equals(eventType)) {
            log.info("event skip");
            return;
        }

        if ("UNBLOCK".equals(action)) {
            unblockEvent(event.eventId());
            return;
        }

        if (activeEvents.containsKey(event.eventId())) {
            unblockEvent(event.eventId());
        }

        Long nodeId = graphNodeRepository.findNearestId(event.lat(), event.lon())
                .orElseThrow(() -> new IllegalStateException("event node error"));

        List<BlockedEdgeRef> blockedEdges = blockOnGraph(graphStorage.getTimeGraph(), nodeId);
        blockOnGraph(graphStorage.getDistanceGraph(), blockedEdges);

        activeEvents.put(event.eventId(), new ActiveTrafficEvent(
                event.eventId(),
                eventType,
                action,
                event.lat(),
                event.lon(),
                event.radiusMeters(),
                event.createdAt(),
                nodeId,
                blockedEdges
        ));

        log.info("event node {}", nodeId);
        log.info("node {} blocked", nodeId);
    }

    private List<BlockedEdgeRef> blockOnGraph(RoadGraph graph, Long nodeId) {
        if (graph == null) {
            throw new IllegalStateException("event graph error");
        }

        RoadNode node = graph.getNodeById(nodeId);
        if (node == null) {
            throw new IllegalStateException("event graph node error");
        }

        List<BlockedEdgeRef> blockedEdges = graph.blockEdgesForNodeWithRefs(nodeId);
        log.info("event blocked {}", blockedEdges.size());
        return blockedEdges;
    }

    private void blockOnGraph(RoadGraph graph, List<BlockedEdgeRef> blockedEdges) {
        if (graph == null) {
            throw new IllegalStateException("event graph error");
        }

        int blockedCount = graph.blockEdges(blockedEdges);
        log.info("event blocked {}", blockedCount);
    }

    private void unblockEvent(String eventId) {
        ActiveTrafficEvent activeEvent = activeEvents.remove(eventId);
        if (activeEvent == null) {
            log.info("event miss");
            return;
        }

        int timeUnblocked = unblockOnGraph(graphStorage.getTimeGraph(), activeEvent.blockedEdges());
        int distanceUnblocked = unblockOnGraph(graphStorage.getDistanceGraph(), activeEvent.blockedEdges());

        log.info("event unblocked {}", timeUnblocked + distanceUnblocked);
        log.info("node {} unblocked", activeEvent.nodeId());
    }

    private int unblockOnGraph(RoadGraph graph, List<BlockedEdgeRef> blockedEdges) {
        if (graph == null) {
            throw new IllegalStateException("event graph error");
        }

        return graph.unblockEdges(blockedEdges);
    }

    private String normalizeAction(String action) {
        if (action == null || action.isBlank()) {
            throw new IllegalStateException("event action error");
        }

        String normalized = action.trim().toUpperCase(Locale.ROOT);
        if (!"BLOCK".equals(normalized) && !"UNBLOCK".equals(normalized)) {
            throw new IllegalStateException("event action error");
        }
        return normalized;
    }

    private String normalizeEventType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalStateException("event type error");
        }

        String normalized = eventType.trim().toUpperCase(Locale.ROOT);
        if (!"ROAD_CLOSED".equals(normalized)
                && !"ACCIDENT".equals(normalized)
                && !"TRAFFIC_JAM".equals(normalized)) {
            throw new IllegalStateException("event type error");
        }
        return normalized;
    }
}
