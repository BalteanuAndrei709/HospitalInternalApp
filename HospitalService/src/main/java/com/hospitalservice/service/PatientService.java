package com.hospitalservice.service;

import com.google.gson.Gson;
import com.hospitalservice.kafka.producer.StateProducer;
import com.hospitalservice.models.dto.PatientStateDTO;
import com.hospitalservice.models.dto.StateDetailsDTO;
import com.hospitalservice.utils.RandomStateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Service
public class PatientService {

    private final StateProducer stateProducer;

    private final RandomStateGenerator randomStateGenerator;

    private final Map<UUID, Future<?>> taskMap = new ConcurrentHashMap<>();

    private final AsyncTaskExecutor taskExecutor;


    @Autowired
    public PatientService(RandomStateGenerator randomStateGenerator,
                          StateProducer stateProducer,
                          @Qualifier("applicationTaskExecutor") AsyncTaskExecutor taskExecutor){
        this.randomStateGenerator = randomStateGenerator;
        this.stateProducer = stateProducer;
        this.taskExecutor = taskExecutor;
    }

    public void connectPatient(UUID patientId) {
        Future<?> task = taskExecutor.submit(() -> startProducingRandomState(patientId));
        taskMap.put(patientId, task);
    }

    @Async
    protected void startProducingRandomState(UUID patientId) {
        try {
            while (!Thread.currentThread().isInterrupted()) {

                StateDetailsDTO randomState = randomStateGenerator.getRandomDetails();
                PatientStateDTO patientStateDTO = createPatientStateDTO(randomState, patientId);

                stateProducer.sendMessage(new Gson().toJson(patientStateDTO));
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            taskMap.remove(patientId); // Clean up the task map
        }
    }

    private PatientStateDTO createPatientStateDTO(StateDetailsDTO randomState, UUID patientId) {
        return PatientStateDTO
                .builder()
                .state(randomState)
                .patientId(patientId)
                .build();
    }

    public void disconnectPatient(UUID patientId) {
        Future<?> task = taskMap.get(patientId);
        if (task != null) {
            task.cancel(true);
        }
    }
}
