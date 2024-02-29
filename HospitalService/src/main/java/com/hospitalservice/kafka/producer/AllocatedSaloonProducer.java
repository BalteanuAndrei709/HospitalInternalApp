package com.hospitalservice.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AllocatedSaloonProducer {

    private final KafkaTemplate<Integer,String> kafkaTemplate;

    @Autowired
    public AllocatedSaloonProducer(KafkaTemplate<Integer, String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String message){
        kafkaTemplate.send(topic,message);
    }
}
