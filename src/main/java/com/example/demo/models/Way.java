package com.example.demo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Way {
    private Long id;
    private String name;
    private String highway;
    private boolean oneway;
    private List<Long> nodes;

    public Way(Long id) {
        this.id = id;
        this.name = null;
        this.highway = null;
        this.oneway = false;
        this.nodes = new ArrayList<>();
    }

    public void addNode(Long nodeId) {
        this.nodes.add(nodeId);
    }

    public boolean isDrivable() {
        if (highway == null) {
            return false;
        }

        return highway.equals("motorway")
                || highway.equals("motorway_link")
                || highway.equals("trunk")
                || highway.equals("trunk_link")
                || highway.equals("primary")
                || highway.equals("primary_link")
                || highway.equals("secondary")
                || highway.equals("secondary_link")
                || highway.equals("tertiary")
                || highway.equals("tertiary_link")
                || highway.equals("unclassified")
                || highway.equals("residential")
                || highway.equals("service")
                || highway.equals("living_street")
                || highway.equals("road");
    }
}
