package com.example.demo.services;

import com.example.demo.models.RoadGraph;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class GraphStorage {
    private RoadGraph timeGraph;
    private RoadGraph distanceGraph;

    public void setGraphs(RoadGraph timeGraph, RoadGraph distanceGraph) {
        this.timeGraph = timeGraph;
        this.distanceGraph = distanceGraph;
    }
}
