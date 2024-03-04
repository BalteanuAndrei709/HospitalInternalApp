package com.emergencyservice.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class EmergencyConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmergencyConsumer.class);


    @KafkaListener(topics = "${kafka.topic.patient.emergency}", groupId = "${kafka.consumer.group.id}")
    public void listen(ConsumerRecord<Integer, String> record, Acknowledgment acknowledgment) {

        try {
            log.info(record.value());

            acknowledgment.acknowledge();
        }
        catch (Exception e){
            log.error(e.getMessage());
            acknowledgment.acknowledge();
        }
    }
}
