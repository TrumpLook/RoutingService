package com.example.demo.services;

import com.example.demo.models.GraphWeightType;
import com.example.demo.models.RoadGraph;
import com.example.demo.models.RoadNode;
import com.example.demo.models.RouteResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AStarServiceTest {

    private final AStarService aStarService = new AStarService();

    @Test
    void shouldFindShortestPathByWeight() {
        RoadGraph graph = new RoadGraph(GraphWeightType.DISTANCE);

        RoadNode a = new RoadNode(1L, 0.0, 0.0);
        RoadNode b = new RoadNode(2L, 0.0, 0.001);
        RoadNode c = new RoadNode(3L, 0.0, 0.002);

        graph.addEdge(a, b, 100.0, 100.0, "road");
        graph.addEdge(b, c, 100.0, 100.0, "road");
        graph.addEdge(a, c, 250.0, 250.0, "road");

        RouteResult result = aStarService.findShortestPath(graph, a, c);

        assertTrue(result.isPathFound());
        assertEquals(200.0, result.getTotalWeight());
        assertEquals(List.of(a, b, c), result.getPath());
    }
}
