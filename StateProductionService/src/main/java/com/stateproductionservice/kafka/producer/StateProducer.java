package com.stateproductionservice.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class StateProducer {

    private final KafkaTemplate<Integer,String> kafkaTemplate;

    @Value("${kafka.topic.patient.state}")
    private String responseTopic;

    @Autowired
    public StateProducer(KafkaTemplate<Integer, String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message){
        kafkaTemplate.send(responseTopic,message);
    }
}
