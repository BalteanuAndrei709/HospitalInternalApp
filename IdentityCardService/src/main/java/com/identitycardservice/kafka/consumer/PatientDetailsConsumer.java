package com.identitycardservice.kafka.consumer;

import com.identitycardservice.service.IdentityCardCheckerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class PatientDetailsConsumer {

    private final IdentityCardCheckerService identityCardCheckerService;

    @Autowired
    public PatientDetailsConsumer(IdentityCardCheckerService identityCardCheckerService){
        this.identityCardCheckerService = identityCardCheckerService;
    }

    private static final Logger log = LoggerFactory.getLogger(PatientDetailsConsumer.class);

    @KafkaListener(topics = "${kafka.topic.patient.details.checker}", groupId = "${kafka.consumer.group.id}")
    public void listen(ConsumerRecord<Integer, String> record, Acknowledgment acknowledgment) {

        try {
            identityCardCheckerService.validateIdentityCardDetails(record.value());

            acknowledgment.acknowledge();
        }
        catch (Exception e){
            log.error(e.getMessage());
            acknowledgment.acknowledge();
        }
    }
}
