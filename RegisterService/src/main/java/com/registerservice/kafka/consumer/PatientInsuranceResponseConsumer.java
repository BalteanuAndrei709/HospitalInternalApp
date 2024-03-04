package com.registerservice.kafka.consumer;

import com.google.gson.Gson;
import com.registerservice.model.dto.ValidationResponseDTO;
import com.registerservice.utils.InsuranceValidationStatusMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class PatientInsuranceResponseConsumer {

    private static final Logger log = LoggerFactory.getLogger(PatientDetailsResponseConsumer.class);

    private final InsuranceValidationStatusMap insuranceValidationStatusMap;

    @Autowired
    public PatientInsuranceResponseConsumer(InsuranceValidationStatusMap insuranceValidationStatusMap){
        this.insuranceValidationStatusMap = insuranceValidationStatusMap;
    }

    @KafkaListener(topics = "${kafka.topic.patient.insurance.checker.response}",
            groupId = "${kafka.consumer.group.id}")
    public void listen(ConsumerRecord<Integer, String> record, Acknowledgment acknowledgment) {
        try {
            ValidationResponseDTO responseDTO = new Gson().fromJson(
                    record.value(),
                    ValidationResponseDTO.class);
            insuranceValidationStatusMap.completeFuture(responseDTO.getIdentifier(),responseDTO.isStatus());

            acknowledgment.acknowledge();
        } catch (Exception e) {
            acknowledgment.acknowledge();
            log.error("Error processing message", e);
        }
    }
}
