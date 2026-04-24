package com.example.demo.models;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class RoadGraph {
    private GraphWeightType weightType;
    private final Map<RoadNode, List<RoadEdge>> adjacencyList = new HashMap<>();
    private final Map<Long, RoadNode> nodeById = new HashMap<>();

    public RoadGraph() {
        this(GraphWeightType.DISTANCE);
    }

    public RoadGraph(GraphWeightType weightType) {
        this.weightType = weightType;
    }

    public void setWeightType(GraphWeightType weightType) {
        this.weightType = weightType;
    }

    public void addNode(RoadNode node) {
        if (!adjacencyList.containsKey(node)) {
            adjacencyList.put(node, new ArrayList<>());
            nodeById.put(node.getId(), node);
        }
    }

    public void addEdge(RoadNode from, RoadNode to, double weight) {
        addNode(from);
        addNode(to);

        RoadEdge edge = new RoadEdge(from, to, weight);
        adjacencyList.get(from).add(edge);
    }

    public void addEdge(RoadNode from, RoadNode to, double weight, double distance, String roadType) {
        addNode(from);
        addNode(to);

        RoadEdge edge = new RoadEdge(from, to, weight, distance, roadType);
        adjacencyList.get(from).add(edge);
    }

    public List<RoadEdge> getEdgesFrom(RoadNode node) {
        return adjacencyList.getOrDefault(node, new ArrayList<>());
    }

    public Set<RoadNode> getNodes() {
        return adjacencyList.keySet();
    }

    public RoadNode getNodeById(Long id) {
        return nodeById.get(id);
    }

    public int getVertexCount() {
        return adjacencyList.size();
    }

    public int getEdgeCount() {
        int count = 0;
        for (List<RoadEdge> edges : adjacencyList.values()) {
            count += edges.size();
        }
        return count;
    }

    public boolean containsNode(RoadNode node) {
        return adjacencyList.containsKey(node);
    }

    public boolean containsNodeById(Long id) {
        return nodeById.containsKey(id);
    }

    public synchronized int blockEdgesForNode(Long nodeId) {
        return blockEdgesForNodeWithRefs(nodeId).size();
    }

    public synchronized List<BlockedEdgeRef> blockEdgesForNodeWithRefs(Long nodeId) {
        RoadNode node = nodeById.get(nodeId);
        if (node == null) {
            return List.of();
        }

        List<BlockedEdgeRef> blockedEdges = new ArrayList<>();
        for (Map.Entry<RoadNode, List<RoadEdge>> entry : adjacencyList.entrySet()) {
            for (RoadEdge edge : entry.getValue()) {
                if (edge.isBlocked()) {
                    continue;
                }

                if (edge.getSource().equals(node) || edge.getTarget().equals(node)) {
                    edge.setBlocked(true);
                    blockedEdges.add(new BlockedEdgeRef(
                            edge.getSource().getId(),
                            edge.getTarget().getId()
                    ));
                }
            }
        }
        return blockedEdges;
    }

    public synchronized int blockEdges(List<BlockedEdgeRef> blockedEdgeRefs) {
        int count = 0;

        for (BlockedEdgeRef blockedEdgeRef : blockedEdgeRefs) {
            RoadNode sourceNode = nodeById.get(blockedEdgeRef.sourceNodeId());
            if (sourceNode == null) {
                continue;
            }

            List<RoadEdge> edges = adjacencyList.getOrDefault(sourceNode, List.of());
            for (RoadEdge edge : edges) {
                if (edge.getSource().getId().equals(blockedEdgeRef.sourceNodeId())
                        && edge.getTarget().getId().equals(blockedEdgeRef.targetNodeId())
                        && !edge.isBlocked()) {
                    edge.setBlocked(true);
                    count++;
                }
            }
        }

        return count;
    }

    public synchronized int unblockEdges(List<BlockedEdgeRef> blockedEdgeRefs) {
        int count = 0;

        for (BlockedEdgeRef blockedEdgeRef : blockedEdgeRefs) {
            RoadNode sourceNode = nodeById.get(blockedEdgeRef.sourceNodeId());
            if (sourceNode == null) {
                continue;
            }

            List<RoadEdge> edges = adjacencyList.getOrDefault(sourceNode, List.of());
            for (RoadEdge edge : edges) {
                if (edge.getSource().getId().equals(blockedEdgeRef.sourceNodeId())
                        && edge.getTarget().getId().equals(blockedEdgeRef.targetNodeId())
                        && edge.isBlocked()) {
                    edge.setBlocked(false);
                    count++;
                }
            }
        }

        return count;
    }

    @Override
    public String toString() {
        return String.format(
                "RoadGraph{weightType=%s, vertices=%d, edges=%d}",
                weightType,
                getVertexCount(),
                getEdgeCount()
        );
    }
}
