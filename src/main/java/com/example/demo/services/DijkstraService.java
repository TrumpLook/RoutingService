package com.example.demo.services;

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
public class DijkstraService {
    private static final Logger log = LoggerFactory.getLogger(DijkstraService.class);

    public RouteResult findShortestPath(RoadGraph graph, Long startNodeId, Long endNodeId) {
        RoadNode startNode = graph.getNodeById(startNodeId);
        RoadNode endNode = graph.getNodeById(endNodeId);

        if (startNode == null || endNode == null) {
            return new RouteResult(List.of(), Double.POSITIVE_INFINITY, 0, 0, 0L, false);
        }

        return findShortestPath(graph, startNode, endNode);
    }

    public RouteResult findShortestPath(RoadGraph graph, RoadNode startNode, RoadNode endNode) {
        log.info("dijkstra start");

        long startTime = System.nanoTime();

        Map<RoadNode, Double> distances = new HashMap<>();
        Map<RoadNode, RoadNode> previousNodes = new HashMap<>();
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>(Comparator.comparingDouble(NodeDistance::distance));
        Set<RoadNode> visited = new HashSet<>();

        for (RoadNode node : graph.getNodes()) {
            distances.put(node, Double.POSITIVE_INFINITY);
        }

        distances.put(startNode, 0.0);
        queue.add(new NodeDistance(startNode, 0.0));

        int relaxedEdges = 0;

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();

            if (!visited.add(current.node())) {
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
                if (visited.contains(neighbor)) {
                    continue;
                }

                double newDistance = distances.get(current.node()) + edge.getWeight();
                if (newDistance < distances.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    distances.put(neighbor, newDistance);
                    previousNodes.put(neighbor, current.node());
                    queue.add(new NodeDistance(neighbor, newDistance));
                    relaxedEdges++;
                }
            }
        }

        long executionTime = System.nanoTime() - startTime;
        double totalWeight = distances.getOrDefault(endNode, Double.POSITIVE_INFINITY);

        if (Double.isInfinite(totalWeight)) {
            log.error("dijkstra error");
            return new RouteResult(List.of(), totalWeight, visited.size(), relaxedEdges, executionTime, false);
        }

        log.info("dijkstra ok");
        List<RoadNode> path = restorePath(previousNodes, endNode);
        return new RouteResult(path, totalWeight, visited.size(), relaxedEdges, executionTime, true);
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

    private record NodeDistance(RoadNode node, double distance) {
    }
}
