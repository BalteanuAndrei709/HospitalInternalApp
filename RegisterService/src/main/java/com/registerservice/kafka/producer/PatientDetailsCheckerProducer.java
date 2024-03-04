package com.registerservice.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PatientDetailsCheckerProducer {

    @Value("${kafka.topic.patient.details.checker}")
    private String patientIdentityCardCheckerTopic;
    private final KafkaTemplate<Integer,String> kafkaTemplate;

    @Autowired
    public PatientDetailsCheckerProducer(KafkaTemplate<Integer, String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message){
        kafkaTemplate.send(patientIdentityCardCheckerTopic,message);
    }
}
