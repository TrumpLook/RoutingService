package com.example.demo.models;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class RouteResult {
    private final List<RoadNode> path;
    private final double totalWeight;
    private final int visitedNodes;
    private final int relaxedEdges;
    private final long executionTimeNanos;
    private final boolean pathFound;

    public RouteResult(List<RoadNode> path,
                       double totalWeight,
                       int visitedNodes,
                       int relaxedEdges,
                       long executionTimeNanos,
                       boolean pathFound) {
        this.path = Collections.unmodifiableList(path);
        this.totalWeight = totalWeight;
        this.visitedNodes = visitedNodes;
        this.relaxedEdges = relaxedEdges;
        this.executionTimeNanos = executionTimeNanos;
        this.pathFound = pathFound;
    }
}
