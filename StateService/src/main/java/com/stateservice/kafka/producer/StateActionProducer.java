package com.stateservice.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class StateActionProducer {

    private final KafkaTemplate<Integer,String> kafkaTemplate;

    @Value("${kafka.topic.patient.state.action}")
    private String topic;

    @Autowired
    public StateActionProducer(KafkaTemplate<Integer, String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message){
        kafkaTemplate.send(topic,message);
    }
}
