package com.insuranceservice.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PatientInsuranceCheckResponse {

    private final KafkaTemplate<Integer,String> kafkaTemplate;

    @Value("${kafka.topic.patient.insurance.checker.response}")
    private String insuranceResponseTopic;

    @Autowired
    public PatientInsuranceCheckResponse(KafkaTemplate<Integer, String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message){
        kafkaTemplate.send(insuranceResponseTopic,message);
    }
}
