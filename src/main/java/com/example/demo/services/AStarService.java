package com.example.demo.services;

import com.example.demo.models.GraphWeightType;
import com.example.demo.models.RoadEdge;
import com.example.demo.models.RoadGraph;
import com.example.demo.models.RoadNode;
import com.example.demo.models.RouteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

@Service
public class AStarService {
    private static final double EARTH_RADIUS = 6371000;
    private static final double MAX_SPEED_METERS_PER_SECOND = 110.0 / 3.6;
    private static final Logger log = LoggerFactory.getLogger(AStarService.class);

    public RouteResult findShortestPath(RoadGraph graph, Long startNodeId, Long endNodeId) {
        RoadNode startNode = graph.getNodeById(startNodeId);
        RoadNode endNode = graph.getNodeById(endNodeId);

        if (startNode == null || endNode == null) {
            return new RouteResult(List.of(), Double.POSITIVE_INFINITY, 0, 0, 0L, false);
        }

        return findShortestPath(graph, startNode, endNode);
    }

    public RouteResult findShortestPath(RoadGraph graph, RoadNode startNode, RoadNode endNode) {
        log.info("astar start");

        long startTime = System.nanoTime();

        Map<RoadNode, Double> gScore = new HashMap<>();
        Map<RoadNode, Double> fScore = new HashMap<>();
        Map<RoadNode, RoadNode> previousNodes = new HashMap<>();
        PriorityQueue<NodeScore> openSet = new PriorityQueue<>(Comparator.comparingDouble(NodeScore::score));
        Set<RoadNode> closedSet = new HashSet<>();

        for (RoadNode node : graph.getNodes()) {
            gScore.put(node, Double.POSITIVE_INFINITY);
            fScore.put(node, Double.POSITIVE_INFINITY);
        }

        gScore.put(startNode, 0.0);
        fScore.put(startNode, heuristic(graph, startNode, endNode));
        openSet.add(new NodeScore(startNode, fScore.get(startNode)));

        int relaxedEdges = 0;

        while (!openSet.isEmpty()) {
            NodeScore current = openSet.poll();

            if (!closedSet.add(current.node())) {
                continue;
            }

            if (current.node().equals(endNode)) {
                break;
            }

            for (RoadEdge edge : graph.getEdgesFrom(current.node())) {
                if (edge.isBlocked()) {
                    continue;
                }

                RoadNode neighbor = edge.getTarget();
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                double tentativeGScore = gScore.get(current.node()) + edge.getWeight();
                if (tentativeGScore < gScore.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    previousNodes.put(neighbor, current.node());
                    gScore.put(neighbor, tentativeGScore);

                    double estimatedScore = tentativeGScore + heuristic(graph, neighbor, endNode);
                    fScore.put(neighbor, estimatedScore);
                    openSet.add(new NodeScore(neighbor, estimatedScore));
                    relaxedEdges++;
                }
            }
        }

        long executionTime = System.nanoTime() - startTime;
        double totalWeight = gScore.getOrDefault(endNode, Double.POSITIVE_INFINITY);

        if (Double.isInfinite(totalWeight)) {
            log.error("astar error");
            return new RouteResult(List.of(), totalWeight, closedSet.size(), relaxedEdges, executionTime, false);
        }

        log.info("astar ok");
        List<RoadNode> path = restorePath(previousNodes, endNode);
        return new RouteResult(path, totalWeight, closedSet.size(), relaxedEdges, executionTime, true);
    }

    private double heuristic(RoadGraph graph, RoadNode fromNode, RoadNode toNode) {
        double straightDistance = haversine(fromNode, toNode);

        if (graph.getWeightType() == GraphWeightType.TIME) {
            return straightDistance / MAX_SPEED_METERS_PER_SECOND;
        }

        return straightDistance;
    }

    private double haversine(RoadNode node1, RoadNode node2) {
        double lat1 = Math.toRadians(node1.getLat());
        double lon1 = Math.toRadians(node1.getLon());
        double lat2 = Math.toRadians(node2.getLat());
        double lon2 = Math.toRadians(node2.getLon());

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon / 2) * Math.sin(dlon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    private List<RoadNode> restorePath(Map<RoadNode, RoadNode> previousNodes, RoadNode endNode) {
        List<RoadNode> path = new ArrayList<>();
        RoadNode current = endNode;

        while (current != null) {
            path.add(current);
            current = previousNodes.get(current);
        }

        Collections.reverse(path);
        return path;
    }

    private record NodeScore(RoadNode node, double score) {
    }
}
