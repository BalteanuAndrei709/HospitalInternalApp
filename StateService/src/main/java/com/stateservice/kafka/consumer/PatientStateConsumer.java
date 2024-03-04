package com.stateservice.kafka.consumer;

import com.stateservice.service.StateService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class PatientStateConsumer {

    private static final Logger log = LoggerFactory.getLogger(PatientStateConsumer.class);

    private final StateService stateService;

    @Autowired
    public PatientStateConsumer(StateService stateService){
        this.stateService = stateService;
    }

    @KafkaListener(topics = "${kafka.topic.patient.state}",
            groupId = "${kafka.consumer.group.id}")
    public void listen(ConsumerRecord<Integer, String> record, Acknowledgment acknowledgment) {
        try {

            stateService.processState(record.value());

            acknowledgment.acknowledge();
        } catch (Exception e) {
            acknowledgment.acknowledge();
            log.error("Error processing message", e);
        }
    }
}
