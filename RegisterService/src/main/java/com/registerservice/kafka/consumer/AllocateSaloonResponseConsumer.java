package com.registerservice.kafka.consumer;

import com.google.gson.Gson;
import com.registerservice.model.dto.SaloonAllocatedResponseDTO;
import com.registerservice.utils.SaloonNumberAllocatedStatusMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class AllocateSaloonResponseConsumer {

    private static final Logger log = LoggerFactory.getLogger(PatientDetailsResponseConsumer.class);

    private final SaloonNumberAllocatedStatusMap saloonNumberAllocatedStatusMap;

    @Autowired
    public AllocateSaloonResponseConsumer(SaloonNumberAllocatedStatusMap saloonNumberAllocatedStatusMap){
        this.saloonNumberAllocatedStatusMap = saloonNumberAllocatedStatusMap;
    }

    @KafkaListener(topics = "${kafka.topic.allocate.saloon.response}",
            groupId = "${kafka.consumer.group.id}")
    public void listen(ConsumerRecord<Integer, String> record, Acknowledgment acknowledgment) {
        try {
            SaloonAllocatedResponseDTO responseDTO = new Gson().fromJson(
                    record.value(),
                    SaloonAllocatedResponseDTO.class);
            saloonNumberAllocatedStatusMap.completeFuture(responseDTO.getIdentifier(),responseDTO.getSaloonNumber());

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }
}
