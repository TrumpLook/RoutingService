package com.example.demo.services;

import com.example.demo.models.GraphNodeEntity;
import com.example.demo.models.RoadGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GraphNodeSyncService {
    private static final Logger log = LoggerFactory.getLogger(GraphNodeSyncService.class);

    private final GraphStorage graphStorage;
    private final GraphNodeRepository graphNodeRepository;

    public GraphNodeSyncService(GraphStorage graphStorage,
                                GraphNodeRepository graphNodeRepository) {
        this.graphStorage = graphStorage;
        this.graphNodeRepository = graphNodeRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncNodes() {
        RoadGraph graph = graphStorage.getTimeGraph();
        if (graph == null || graph.getNodes().isEmpty()) {
            log.info("db skip");
            return;
        }

        List<GraphNodeEntity> graphNodes = new ArrayList<>(graph.getNodes().size());
        graph.getNodes().forEach(node -> graphNodes.add(new GraphNodeEntity(
                node.getId(),
                node.getLat(),
                node.getLon()
        )));

        graphNodeRepository.saveAll(graphNodes);
        log.info("db nodes {}", graphNodes.size());
    }
}
