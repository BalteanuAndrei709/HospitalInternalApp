package com.registerservice.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PatientInsuranceCheckerProducer {

    @Value("${kafka.topic.patient.insurance.checker}")
    private String patientInsuranceCheckerTopic;

    private final KafkaTemplate<Integer,String> kafkaTemplate;

    @Autowired
    public PatientInsuranceCheckerProducer(KafkaTemplate<Integer, String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message){
        kafkaTemplate.send(patientInsuranceCheckerTopic,message);
    }
}
