package com.example.demo.models;

import lombok.Data;

@Data
public class RoadEdge {
    private RoadNode source;
    private RoadNode target;
    private double weight;
    private double distance;
    private String roadType;
    private boolean blocked;

    public RoadEdge(RoadNode source, RoadNode target, double weight) {
        this(source, target, weight, 0.0, null, false);
    }

    public RoadEdge(RoadNode source, RoadNode target, double weight, double distance, String roadType) {
        this(source, target, weight, distance, roadType, false);
    }

    public RoadEdge(RoadNode source, RoadNode target, double weight, double distance, String roadType, boolean blocked) {
        this.source = source;
        this.target = target;
        this.weight = weight;
        this.distance = distance;
        this.roadType = roadType;
        this.blocked = blocked;
    }
}
