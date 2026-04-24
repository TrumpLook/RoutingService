package com.example.demo.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class RoadNode {
    private Long id;
    private Double lat;
    private Double lon;

}
