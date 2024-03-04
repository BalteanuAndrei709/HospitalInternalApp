package com.registerservice.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AllocateSaloonCheckerProducer {

    @Value("${kafka.topic.allocate.saloon}")
    private String allocateSaloonTopic;

    private final KafkaTemplate<Integer,String> kafkaTemplate;

    @Autowired
    public AllocateSaloonCheckerProducer(KafkaTemplate<Integer, String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message){
        kafkaTemplate.send(allocateSaloonTopic,message);
    }
}
