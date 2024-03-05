package com.stateproductionservice.service;

import com.google.gson.Gson;
import com.stateproductionservice.kafka.producer.StateProducer;
import com.stateproductionservice.models.Action;
import com.stateproductionservice.models.dto.ActionStateDTO;
import com.stateproductionservice.models.dto.StateDetailsDTO;
import com.stateproductionservice.repository.ActionRepository;
import com.stateproductionservice.utils.RandomStateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class StateActionService {

    private final ActionRepository actionRepository;

    private final RandomStateGenerator randomStateGenerator;

    private final StateProducer stateProducer;

    private final Gson gson = new Gson();

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final ConcurrentHashMap<UUID, Future<?>> activeTasks = new ConcurrentHashMap<>();

    @Autowired
    public StateActionService(ActionRepository actionRepository,
                              RandomStateGenerator randomStateGenerator,
                              StateProducer stateProducer){
        this.actionRepository = actionRepository;
        this.randomStateGenerator = randomStateGenerator;
        this.stateProducer = stateProducer;
    }
    public void processAction(ActionStateDTO actionStateDTO) {

        Action action = persistAction(actionStateDTO);

        doAction(action);
    }

    private void doAction(Action action) {

        UUID patientId = action.getPatientId();
        // Check the last command from the database for this patientId
        String lastCommand = actionRepository.getTopByPatientIdOrderByTimestampDesc(patientId).getAction();

        // If the command is "start"
        if("start".equals(lastCommand)){
            startProducingData(action.getPatientId());
        }
        else if("stop".equals(lastCommand)){
            stopProducingData(patientId);
        }
    }

    public void stopProducingData(UUID patientId){
        Future<?> task = activeTasks.remove(patientId); // Remove the task from active tasks
        if(task != null) {
            task.cancel(true); // This attempts to interrupt the running thread
        }
    }

    private void startProducingData(UUID patientId) {

        if(activeTasks.containsKey(patientId)) {
            return; // A task is already running for this patientId, so we do nothing
        }

        // Submit a new task for producing data for this patient
        Future<?> task = executorService.submit(() -> {
            produceDataForPatient(patientId);
        });

        // Keep track of the task so we can stop it later if needed
        activeTasks.put(patientId, task);
    }

    private void produceDataForPatient(UUID patientId) {

        while (!Thread.currentThread().isInterrupted()) {
            // Example: Generate and process data
            try {
                // Simulate data production
                StateDetailsDTO stateDetailsDTO = randomStateGenerator.getRandomDetails();

                String message = createStateMessage(patientId, stateDetailsDTO);

                stateProducer.sendMessage(message);

                Thread.sleep(1000); // Sleep to simulate time taken to produce data
                System.out.println("Producing data for patient: " + patientId);
            } catch (InterruptedException e) {
                // Restore the interrupted status
                Thread.currentThread().interrupt();
            }
        }
    }

    private String createStateMessage(UUID patientId, StateDetailsDTO stateDetailsDTO){

        Map<String, Object> messageMap = new HashMap<>();

        messageMap.put("patientId",patientId);
        messageMap.put("state", stateDetailsDTO);

        return gson.toJson(messageMap);
    }
    private Action persistAction(ActionStateDTO actionStateDTO) {

        Action action = Action
                .builder()
                .action(actionStateDTO.getAction())
                .patientId(actionStateDTO.getPatientId())
                .timestamp(Date.from(Instant.now()))
                .build();

        return actionRepository.save(action);
    }
}
