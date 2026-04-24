package com.example.demo.configs;

import com.example.demo.models.RoadGraph;
import com.example.demo.services.GraphBuilder;
import com.example.demo.services.GraphStorage;
import com.example.demo.services.OSMParser;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;

@Component
@ConditionalOnProperty(name = "app.load-map-on-startup", havingValue = "true", matchIfMissing = true)
public class StartConfig {
    private static final Logger log = LoggerFactory.getLogger(StartConfig.class);

    private final GraphStorage graphStorage;

    public StartConfig(GraphStorage graphStorage) {
        this.graphStorage = graphStorage;
    }

    @SneakyThrows
    @PostConstruct
    public void init() throws ParserConfigurationException, SAXException {
        log.info("map load");

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        OSMParser handler = new OSMParser();
        saxParser.parse(new File("Moscow.osm"), handler);

        RoadGraph timeGraph = GraphBuilder.buildTimeGraph(
                handler.getListOfNodes(),
                handler.getListOfWays(),
                handler.getNodeMap()
        );

        RoadGraph distanceGraph = GraphBuilder.buildDistanceGraph(
                handler.getListOfNodes(),
                handler.getListOfWays(),
                handler.getNodeMap()
        );

        graphStorage.setGraphs(timeGraph, distanceGraph);

        log.info("map nodes {}", handler.getListOfNodes().size());
        log.info("map ways {}", handler.getListOfWays().size());
        log.info("graph time {}", timeGraph);
        log.info("graph distance {}", distanceGraph);
    }
}
