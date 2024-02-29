package com.identitycardcheckerservice.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CheckerResponseProducer {

    private final KafkaTemplate<Integer,String> kafkaTemplate;

    @Autowired
    public CheckerResponseProducer(KafkaTemplate<Integer, String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String message){
        kafkaTemplate.send(topic,message);
    }
}
