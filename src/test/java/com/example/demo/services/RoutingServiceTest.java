package com.example.demo.services;

import com.example.demo.dto.RouteResponse;
import com.example.demo.models.GraphWeightType;
import com.example.demo.models.RoadGraph;
import com.example.demo.models.RoadNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutingServiceTest {

    @Mock
    private GraphNodeRepository graphNodeRepository;

    private GraphStorage graphStorage;
    private RoutingService routingService;

    private RoadNode a;
    private RoadNode b;
    private RoadNode c;
    private RoadNode d;
    private RoadNode e;
    private RoadNode x;
    private RoadNode y;

    @BeforeEach
    void setUp() {
        graphStorage = new GraphStorage();
        routingService = new RoutingService(
                graphStorage,
                new DijkstraService(),
                new AStarService(),
                graphNodeRepository
        );

        a = new RoadNode(1L, 0.0, 0.0);
        b = new RoadNode(2L, 0.0, 1.0);
        c = new RoadNode(3L, 0.0, 2.0);
        d = new RoadNode(4L, 0.0, 3.0);
        e = new RoadNode(5L, 0.0, 4.0);
        x = new RoadNode(6L, 1.0, 1.5);
        y = new RoadNode(7L, 1.0, 2.5);

        RoadGraph distanceGraph = new RoadGraph(GraphWeightType.DISTANCE);
        addBidirectionalEdge(distanceGraph, a, b, 1.0);
        addBidirectionalEdge(distanceGraph, b, c, 1.0);
        addBidirectionalEdge(distanceGraph, c, d, 1.0);
        addBidirectionalEdge(distanceGraph, d, e, 1.0);
        addBidirectionalEdge(distanceGraph, b, x, 1.0);
        addBidirectionalEdge(distanceGraph, x, y, 1.0);
        addBidirectionalEdge(distanceGraph, y, d, 1.0);

        RoadGraph timeGraph = new RoadGraph(GraphWeightType.TIME);
        addBidirectionalEdge(timeGraph, a, b, 1.0);
        addBidirectionalEdge(timeGraph, b, c, 1.0);
        addBidirectionalEdge(timeGraph, c, d, 1.0);
        addBidirectionalEdge(timeGraph, d, e, 1.0);
        addBidirectionalEdge(timeGraph, b, x, 1.0);
        addBidirectionalEdge(timeGraph, x, y, 1.0);
        addBidirectionalEdge(timeGraph, y, d, 1.0);

        graphStorage.setGraphs(timeGraph, distanceGraph);
    }

    @Test
    void shouldReplaceBlockedSubrouteLocally() {
        blockBothDirections(graphStorage.getDistanceGraph(), b, c);
        blockBothDirections(graphStorage.getDistanceGraph(), c, d);

        LocalReplanRequest request = new LocalReplanRequest(
                a.getLat(),
                a.getLon(),
                e.getLat(),
                e.getLon(),
                null,
                "DIJKSTRA",
                "DISTANCE",
                List.of(a.getId(), b.getId(), c.getId(), d.getId(), e.getId()),
                List.of(
                        new RouteResponse.RoutePoint(a.getLat(), a.getLon()),
                        new RouteResponse.RoutePoint(e.getLat(), e.getLon())
                ),
                List.of(1)
        );

        RouteResponse response = routingService.replanLocal(request);

        assertTrue(response.pathFound());
        assertEquals(List.of(1L, 2L, 6L, 7L, 4L, 5L), response.nodePath());
    }

    @Test
    void shouldKeepRouteWhenBlockedSegmentIsAbsent() {
        LocalReplanRequest request = new LocalReplanRequest(
                a.getLat(),
                a.getLon(),
                e.getLat(),
                e.getLon(),
                null,
                "DIJKSTRA",
                "DISTANCE",
                List.of(a.getId(), b.getId(), c.getId(), d.getId(), e.getId()),
                List.of(
                        new RouteResponse.RoutePoint(a.getLat(), a.getLon()),
                        new RouteResponse.RoutePoint(e.getLat(), e.getLon())
                ),
                List.of(1)
        );

        RouteResponse response = routingService.replanLocal(request);

        assertTrue(response.pathFound());
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), response.nodePath());
        assertEquals(4.0, response.totalWeight());
    }

    @Test
    void shouldFallbackToFullRouteWhenCurrentPathIsInvalid() {
        when(graphNodeRepository.findNearestId(a.getLat(), a.getLon())).thenReturn(Optional.of(a.getId()));
        when(graphNodeRepository.findNearestId(e.getLat(), e.getLon())).thenReturn(Optional.of(e.getId()));

        LocalReplanRequest request = new LocalReplanRequest(
                a.getLat(),
                a.getLon(),
                e.getLat(),
                e.getLon(),
                null,
                "DIJKSTRA",
                "DISTANCE",
                List.of(a.getId()),
                List.of(),
                List.of()
        );

        RouteResponse response = routingService.replanLocal(request);

        assertTrue(response.pathFound());
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), response.nodePath());
    }

    private void addBidirectionalEdge(RoadGraph graph, RoadNode from, RoadNode to, double weight) {
        graph.addEdge(from, to, weight, weight, "road");
        graph.addEdge(to, from, weight, weight, "road");
    }

    private void blockBothDirections(RoadGraph graph, RoadNode from, RoadNode to) {
        graph.findEdge(from.getId(), to.getId()).setBlocked(true);
        graph.findEdge(to.getId(), from.getId()).setBlocked(true);
    }
}
