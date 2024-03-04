package com.hospitalservice.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AllocatedSaloonProducer {

    private final KafkaTemplate<Integer,String> kafkaTemplate;

    @Value("${kafka.topic.allocate.saloon.response}")
    private String responseTopic;

    @Autowired
    public AllocatedSaloonProducer(KafkaTemplate<Integer, String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message){
        kafkaTemplate.send(responseTopic,message);
    }
}
