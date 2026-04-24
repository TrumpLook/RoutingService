package com.example.demo.services;
import javax.xml.parsers.*;

import com.example.demo.models.RoadNode;
import com.example.demo.models.Way;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import java.util.*;

@Getter
@Setter
@Service
public class OSMParser extends DefaultHandler {

    private ArrayList<RoadNode> listOfNodes = new ArrayList<>();
    private ArrayList<Way> listOfWays = new ArrayList<>();
    private Way currentWay = null;

    // Для быстрого поиска узла по ID (пригодится при построении графа)
    private Map<Long, RoadNode> nodeMap = new HashMap<>();

    public OSMParser() throws ParserConfigurationException, SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("node")) {
            String id = attributes.getValue("id");
            Long longId = Long.parseLong(id);
            String lat = attributes.getValue("lat");
            Double doubleLat = Double.parseDouble(lat);
            String lon = attributes.getValue("lon");
            Double doubleLon = Double.parseDouble(lon);

            RoadNode newNode = new RoadNode(longId, doubleLat, doubleLon);
            listOfNodes.add(newNode);
            nodeMap.put(longId, newNode);
        }

        if (qName.equals("way")) {
            String id = attributes.getValue("id");
            Long longId = Long.parseLong(id);
            currentWay = new Way(longId);
        }

        if (qName.equals("nd") && currentWay != null) {
            String nodeId = attributes.getValue("ref");
            Long longNodeId = Long.parseLong(nodeId);
            currentWay.addNode(longNodeId);
        }

        if (qName.equals("tag") && currentWay != null) {
            String key = attributes.getValue("k");
            String value = attributes.getValue("v");

            switch (key) {
                case "name":
                    currentWay.setName(value);
                    break;
                case "highway":
                    currentWay.setHighway(value);
                    break;
                case "oneway":
                    // oneway может быть "yes", "no", "-1" (реверсивная)
                    currentWay.setOneway("yes".equals(value));
                    break;
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("way")) {
            if (currentWay != null && currentWay.isDrivable()) {
                listOfWays.add(currentWay);
            }
            currentWay = null;
        }
    }

    // Геттер для nodeMap
    public Map<Long, RoadNode> getNodeMap() {
        return nodeMap;
    }
}
