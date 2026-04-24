package com.example.demo.services;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaTrafficEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaTrafficEventConsumer.class);

    private final TrafficEventService trafficEventService;

    public KafkaTrafficEventConsumer(TrafficEventService trafficEventService) {
        this.trafficEventService = trafficEventService;
    }

    @PostConstruct
    public void ready() {
        log.info("event listener ready");
    }

    @KafkaListener(topics = "${app.kafka.traffic-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String payload) {
        log.info("event recv");
        trafficEventService.handleKafkaMessage(payload);
    }
}
