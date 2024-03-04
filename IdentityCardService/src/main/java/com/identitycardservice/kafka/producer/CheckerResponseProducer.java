package com.identitycardservice.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CheckerResponseProducer {

    private final KafkaTemplate<Integer,String> kafkaTemplate;


    @Value("${kafka.topic.patient.details.checker.response}")
    private String checkerResponseTopic;

    @Autowired
    public CheckerResponseProducer(KafkaTemplate<Integer, String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message){
        kafkaTemplate.send(checkerResponseTopic,message);
    }
}
