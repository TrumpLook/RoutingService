package com.example.demo.services;

import com.example.demo.models.RoadGraph;
import com.example.demo.models.RoadNode;
import com.example.demo.models.RouteResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DijkstraServiceTest {

    private final DijkstraService dijkstraService = new DijkstraService();

    @Test
    void shouldFindShortestPathByWeight() {
        RoadGraph graph = new RoadGraph();

        RoadNode a = new RoadNode(1L, 0.0, 0.0);
        RoadNode b = new RoadNode(2L, 0.0, 1.0);
        RoadNode c = new RoadNode(3L, 1.0, 1.0);

        graph.addEdge(a, b, 5.0, 100.0, "road");
        graph.addEdge(b, c, 3.0, 100.0, "road");
        graph.addEdge(a, c, 15.0, 300.0, "road");

        RouteResult result = dijkstraService.findShortestPath(graph, a, c);

        assertTrue(result.isPathFound());
        assertEquals(8.0, result.getTotalWeight());
        assertEquals(List.of(a, b, c), result.getPath());
    }
}
