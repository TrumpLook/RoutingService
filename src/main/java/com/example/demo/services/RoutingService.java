package com.example.demo.services;

import com.example.demo.dto.RouteRequest;
import com.example.demo.dto.RouteResponse;
import com.example.demo.models.GraphWeightType;
import com.example.demo.models.RoadGraph;
import com.example.demo.models.RoadNode;
import com.example.demo.models.RouteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class RoutingService {
    private static final double EARTH_RADIUS = 6371000;
    private static final Logger log = LoggerFactory.getLogger(RoutingService.class);

    private final GraphStorage graphStorage;
    private final DijkstraService dijkstraService;
    private final AStarService aStarService;
    private final GraphNodeRepository graphNodeRepository;

    public RoutingService(GraphStorage graphStorage,
                          DijkstraService dijkstraService,
                          AStarService aStarService,
                          GraphNodeRepository graphNodeRepository) {
        this.graphStorage = graphStorage;
        this.dijkstraService = dijkstraService;
        this.aStarService = aStarService;
        this.graphNodeRepository = graphNodeRepository;
    }

    public RouteResponse buildRoute(RouteRequest request) {
        log.info("route request");

        RoadGraph graph = selectGraph(request.weightType());
        ensureGraphIsLoaded(graph);

        RoadNode startNode = findNearestNode(graph, request.startLat(), request.startLon());
        RoadNode endNode = findNearestNode(graph, request.endLat(), request.endLon());

        log.info("route nodes {} {}", startNode.getId(), endNode.getId());

        RouteResult routeResult = runAlgorithm(
                graph,
                startNode,
                endNode,
                request.algorithm()
        );

        if (!routeResult.isPathFound()) {
            log.error("route not found");
        }

        return new RouteResponse(
                routeResult.isPathFound(),
                routeResult.getTotalWeight(),
                routeResult.getVisitedNodes(),
                routeResult.getRelaxedEdges(),
                routeResult.getExecutionTimeNanos(),
                toPoints(routeResult.getPath())
        );
    }

    private RoadGraph selectGraph(String weightTypeValue) {
        GraphWeightType weightType = parseWeightType(weightTypeValue);
        return weightType == GraphWeightType.TIME
                ? graphStorage.getTimeGraph()
                : graphStorage.getDistanceGraph();
    }

    private GraphWeightType parseWeightType(String value) {
        if (value == null || value.isBlank()) {
            return GraphWeightType.TIME;
        }

        try {
            return GraphWeightType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported weightType: " + value);
        }
    }

    private RouteResult runAlgorithm(RoadGraph graph, RoadNode startNode, RoadNode endNode, String algorithm) {
        String normalizedAlgorithm = algorithm == null ? "DIJKSTRA" : algorithm.trim().toUpperCase(Locale.ROOT);
        log.info("route algo {}", normalizedAlgorithm);

        return switch (normalizedAlgorithm) {
            case "DIJKSTRA" -> dijkstraService.findShortestPath(graph, startNode, endNode);
            case "ASTAR", "A*" -> aStarService.findShortestPath(graph, startNode, endNode);
            default -> throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        };
    }

    private void ensureGraphIsLoaded(RoadGraph graph) {
        if (graph == null || graph.getNodes().isEmpty()) {
            throw new IllegalStateException("Graph is not loaded");
        }
    }

    private RoadNode findNearestNode(RoadGraph graph, double lat, double lon) {
        try {
            Long nodeId = graphNodeRepository.findNearestId(lat, lon)
                    .orElseThrow(() -> new IllegalStateException("No nodes in database"));
            RoadNode node = graph.getNodeById(nodeId);
            if (node != null) {
                log.info("nearest db {}", nodeId);
                return node;
            }
            log.error("nearest db miss");
        } catch (RuntimeException exception) {
            log.error("nearest db error");
        }

        RoadNode nearestNode = null;
        double bestDistance = Double.POSITIVE_INFINITY;

        for (RoadNode node : graph.getNodes()) {
            double currentDistance = haversine(lat, lon, node.getLat(), node.getLon());
            if (currentDistance < bestDistance) {
                bestDistance = currentDistance;
                nearestNode = node;
            }
        }

        if (nearestNode == null) {
            throw new IllegalStateException("No nodes available in graph");
        }

        log.info("nearest mem {}", nearestNode.getId());
        return nearestNode;
    }

    private List<RouteResponse.RoutePoint> toPoints(List<RoadNode> path) {
        List<RouteResponse.RoutePoint> points = new ArrayList<>(path.size());
        for (RoadNode node : path) {
            points.add(new RouteResponse.RoutePoint(node.getLat(), node.getLon()));
        }
        return points;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double latRad1 = Math.toRadians(lat1);
        double lonRad1 = Math.toRadians(lon1);
        double latRad2 = Math.toRadians(lat2);
        double lonRad2 = Math.toRadians(lon2);

        double dlat = latRad2 - latRad1;
        double dlon = lonRad2 - lonRad1;

        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
                + Math.cos(latRad1) * Math.cos(latRad2) * Math.sin(dlon / 2) * Math.sin(dlon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }
}
