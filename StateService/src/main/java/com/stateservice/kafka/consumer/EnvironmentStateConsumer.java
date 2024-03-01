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
public class EnvironmentStateConsumer {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentStateConsumer.class);

    private final StateService stateService;

    @Autowired
    public EnvironmentStateConsumer(StateService stateService){
        this.stateService = stateService;
    }

    @KafkaListener(topics = "${kafka.topic.patient.state.start}",
            groupId = "${kafka.consumer.group.id}")
    public void listen(ConsumerRecord<Integer, String> record, Acknowledgment acknowledgment) {
        try {

            stateService.createEnvironmentStateForPatient(record.value());

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }
}
