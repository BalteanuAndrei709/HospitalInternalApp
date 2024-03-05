package com.stateproductionservice.kafka.consumer;

import com.google.gson.Gson;
import com.stateproductionservice.models.dto.ActionStateDTO;
import com.stateproductionservice.service.StateActionService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class StateActionConsumer {

    private static final Logger log = LoggerFactory.getLogger(StateActionConsumer.class);

    private final StateActionService stateActionService;

    @Autowired
    public StateActionConsumer(StateActionService stateActionService){
        this.stateActionService = stateActionService;
    }

    @KafkaListener(topics = "${kafka.topic.patient.state.action}",
            groupId = "${kafka.consumer.group.id}")
    public void listen(ConsumerRecord<Integer, String> record, Acknowledgment acknowledgment) {
        try {

            ActionStateDTO actionStateDTO = new Gson().fromJson(record.value(),ActionStateDTO.class);

            stateActionService.processAction(actionStateDTO);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            acknowledgment.acknowledge();
            log.error("Error processing message", e);
        }
    }
}
