package com.insuranceservice.kafka.consumer;

import com.google.gson.Gson;
import com.insuranceservice.model.dto.InsuranceDetailsDTO;
import com.insuranceservice.service.InsuranceService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class PatientInsuranceConsumer {

    private static final Logger log = LoggerFactory.getLogger(PatientInsuranceConsumer.class);

    private final InsuranceService insuranceService;

    @Autowired
    public PatientInsuranceConsumer(InsuranceService insuranceService){
        this.insuranceService = insuranceService;
    }

    @KafkaListener(topics = "${kafka.topic.patient.insurance.checker}",
            groupId = "${kafka.consumer.group.id}")
    public void listen(ConsumerRecord<Integer, String> record, Acknowledgment acknowledgment) {
        try {
            InsuranceDetailsDTO insuranceDetails = new Gson().fromJson(record.value(), InsuranceDetailsDTO.class);
            log.info(insuranceDetails.toString());
            insuranceService.checkInsurance(insuranceDetails);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            acknowledgment.acknowledge();
            log.error("Error processing message", e);
        }
    }
}
