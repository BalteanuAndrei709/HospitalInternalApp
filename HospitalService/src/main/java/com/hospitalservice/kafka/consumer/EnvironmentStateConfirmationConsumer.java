package com.hospitalservice.kafka.consumer;

import com.google.gson.Gson;
import com.hospitalservice.models.dto.ValidationResponseDTO;
import com.hospitalservice.utils.EnvironmentStateStatusMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentStateConfirmationConsumer {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentStateConfirmationConsumer.class);

    private final EnvironmentStateStatusMap environmentStateStatusMap;

    @Autowired
    public EnvironmentStateConfirmationConsumer(EnvironmentStateStatusMap environmentStateStatusMap){
        this.environmentStateStatusMap = environmentStateStatusMap;
    }

    @KafkaListener(topics = "${kafka.topic.patient.state.start.response}",
            groupId = "${kafka.consumer.group.id}")
    public void listen(ConsumerRecord<Integer, String> record, Acknowledgment acknowledgment) {
        try {
            ValidationResponseDTO validationResponse = new Gson().fromJson(record.value(), ValidationResponseDTO.class);
            environmentStateStatusMap.completeFuture(validationResponse.getIdentifier(),validationResponse.getStatus());

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }
}
