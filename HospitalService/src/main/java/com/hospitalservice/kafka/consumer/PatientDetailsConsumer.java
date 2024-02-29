package com.hospitalservice.kafka.consumer;

import com.hospitalservice.service.PatientAllocationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class PatientDetailsConsumer {

    private final PatientAllocationService patientAllocationService;

    private static final Logger log = LoggerFactory.getLogger(PatientDetailsConsumer.class);

    @Autowired
    public PatientDetailsConsumer(PatientAllocationService patientAllocationService){
        this.patientAllocationService = patientAllocationService;
    }

    @KafkaListener(topics = "${kafka.topic.allocate.saloon}", groupId = "${kafka.consumer.group.id}")
    public void listen(ConsumerRecord<Integer, String> record, Acknowledgment acknowledgment) {

        try {
            patientAllocationService.allocateSaloon(record.value());

            acknowledgment.acknowledge();
        }
        catch (Exception e){
            log.error(e.getMessage());
            acknowledgment.acknowledge();
        }
    }
}
