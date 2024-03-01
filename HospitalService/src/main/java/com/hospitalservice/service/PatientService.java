package com.hospitalservice.service;

import com.google.gson.Gson;
import com.hospitalservice.kafka.consumer.EnvironmentStateConfirmationConsumer;
import com.hospitalservice.kafka.producer.NewPatientProducer;
import com.hospitalservice.kafka.producer.StateProducer;
import com.hospitalservice.models.dto.PatientStateDTO;
import com.hospitalservice.models.dto.StateDetailsDTO;
import com.hospitalservice.utils.EnvironmentStateStatusMap;
import com.hospitalservice.utils.RandomStateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class PatientService {

    private final NewPatientProducer newPatientProducer;

    private final StateProducer stateProducer;

    private final EnvironmentStateStatusMap environmentStateStatusMap;

    private final RandomStateGenerator randomStateGenerator;

    private static final Logger log = LoggerFactory.getLogger(EnvironmentStateConfirmationConsumer.class);


    @Autowired
    public PatientService(NewPatientProducer newPatientProducer,
                          EnvironmentStateStatusMap environmentStateStatusMap,
                          RandomStateGenerator randomStateGenerator,
                          StateProducer stateProducer){
        this.newPatientProducer = newPatientProducer;
        this.environmentStateStatusMap = environmentStateStatusMap;
        this.randomStateGenerator = randomStateGenerator;
        this.stateProducer = stateProducer;
    }

    public String connectPatient(UUID patientId) {
            
        boolean patientStateEnvironmentCreated = createEnvironment(patientId);

        if (!patientStateEnvironmentCreated){
            return "Error at creating environment for patient.";
        }

        startProducingRandomState(patientId);

        return "Patient environment setup and state production started.";
    }

    @Async
    protected void startProducingRandomState(UUID patientId) {
        try {
            while (true) {
                
                StateDetailsDTO randomState = randomStateGenerator.getRandomDetails();
                PatientStateDTO patientStateDTO = createPatientStateDTO(randomState, patientId);

                stateProducer.sendMessage(new Gson().toJson(patientStateDTO));
                Thread.sleep(5000); // Pause for 5 seconds before the next production
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage());
        }
    }

    private PatientStateDTO createPatientStateDTO(StateDetailsDTO randomState, UUID patientId) {
        return PatientStateDTO
                .builder()
                .state(randomState)
                .patientId(patientId)
                .build();
    }

    private boolean createEnvironment(UUID patientId) {
        
        newPatientProducer.sendMessage(patientId.toString());
        
        return waitForEnvironmentStateConfirmation(patientId);
    }

    private boolean waitForEnvironmentStateConfirmation(UUID patientId) {

        CompletableFuture<Boolean> future = environmentStateStatusMap.getOrCreateFuture(patientId);

        try {
            // Wait for the future to complete with a timeout
            return future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for validation", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Exception occurred during validation", e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Validation timed out", e);
        }
    }
}
