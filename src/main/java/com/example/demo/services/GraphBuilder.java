package com.example.demo.services;

import com.example.demo.models.GraphWeightType;
import com.example.demo.models.RoadGraph;
import com.example.demo.models.RoadNode;
import com.example.demo.models.Way;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphBuilder {

    private static final double EARTH_RADIUS = 6371000;

    public static RoadGraph buildTimeGraph(ArrayList<RoadNode> nodes,
                                           ArrayList<Way> ways,
                                           Map<Long, RoadNode> nodeMap) {
        return buildGraph(nodes, ways, nodeMap, GraphWeightType.TIME);
    }

    public static RoadGraph buildDistanceGraph(ArrayList<RoadNode> nodes,
                                               ArrayList<Way> ways,
                                               Map<Long, RoadNode> nodeMap) {
        return buildGraph(nodes, ways, nodeMap, GraphWeightType.DISTANCE);
    }

    public static RoadGraph buildGraph(ArrayList<RoadNode> nodes,
                                       ArrayList<Way> ways,
                                       Map<Long, RoadNode> nodeMap,
                                       GraphWeightType weightType) {
        RoadGraph graph = new RoadGraph(weightType);

        for (Way way : ways) {
            List<Long> nodeRefs = way.getNodes();
            if (nodeRefs.size() < 2) {
                continue;
            }

            double speedKmh = getSpeedForHighway(way.getHighway());

            for (int i = 0; i < nodeRefs.size() - 1; i++) {
                RoadNode fromNode = nodeMap.get(nodeRefs.get(i));
                RoadNode toNode = nodeMap.get(nodeRefs.get(i + 1));

                if (fromNode == null || toNode == null) {
                    continue;
                }

                double distanceMeters = haversine(fromNode, toNode);
                double timeSeconds = distanceMeters / (speedKmh / 3.6);
                double weight = weightType == GraphWeightType.TIME ? timeSeconds : distanceMeters;

                graph.addEdge(fromNode, toNode, weight, distanceMeters, way.getHighway());

                if (!way.isOneway()) {
                    graph.addEdge(toNode, fromNode, weight, distanceMeters, way.getHighway());
                }
            }
        }

        return graph;
    }

    private static double getSpeedForHighway(String highway) {
        if (highway == null) {
            return 40;
        }

        switch (highway) {
            case "motorway":
            case "motorway_link":
                return 110;
            case "trunk":
            case "trunk_link":
                return 100;
            case "primary":
            case "primary_link":
                return 80;
            case "secondary":
            case "secondary_link":
                return 60;
            case "tertiary":
            case "tertiary_link":
                return 50;
            case "unclassified":
                return 45;
            case "residential":
            case "service":
                return 30;
            case "living_street":
                return 20;
            case "road":
                return 40;
            default:
                return 40;
        }
    }

    private static double haversine(RoadNode node1, RoadNode node2) {
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
}
