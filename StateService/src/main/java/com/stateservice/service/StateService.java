package com.stateservice.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stateservice.kafka.producer.EmergencyProducer;
import com.stateservice.kafka.producer.StateActionProducer;
import com.stateservice.models.Patient;
import com.stateservice.models.Saloon;
import com.stateservice.models.State;
import com.stateservice.models.dto.PatientStateDTO;
import com.stateservice.models.response.PatientStateResponse;
import com.stateservice.repository.PatientRepository;
import com.stateservice.repository.SaloonRepository;
import com.stateservice.repository.StateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StateService {

    private static final Logger log = LoggerFactory.getLogger(StateService.class);

    private final Gson gson = new Gson();

    private final StateRepository stateRepository;

    private final PatientRepository patientRepository;

    private final SaloonRepository saloonRepository;

    private final EmergencyProducer emergencyProducer;

    private final StateActionProducer stateActionProducer;

    @Autowired
    public StateService(PatientRepository patientRepository,
                        StateRepository stateRepository,
                        SaloonRepository saloonRepository,
                        EmergencyProducer emergencyProducer,
                        StateActionProducer stateActionProducer) {
        this.patientRepository = patientRepository;
        this.stateRepository = stateRepository;
        this.saloonRepository = saloonRepository;
        this.emergencyProducer = emergencyProducer;
        this.stateActionProducer = stateActionProducer;
    }

    public void processState(String value) {

        Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy, hh:mm:ss a").create();
        PatientStateDTO patientStateDTO = gson.fromJson(value, PatientStateDTO.class);

        State state = persistState(patientStateDTO);

        if(state.isEmergency()){
            sendEmergencyAlert(state.getPatient());
        }
    }

    public List<PatientStateResponse> getAllPatientsState() {

        List<PatientStateResponse> response = new ArrayList<>();

        List<Saloon> allSaloons = saloonRepository.findAll();

        allSaloons.forEach(s -> {
            List<Patient> allPatientsInSaloon = s.getPatientList();

            if(!allPatientsInSaloon.isEmpty()){

                allPatientsInSaloon.forEach(patient -> {
                    State patientState = stateRepository.findTopByPatientIdOrderByTimestampDesc(patient.getId());

                    if(patientState != null){
                        response.add(PatientStateResponse
                                .builder()
                                .glucose(patientState.getGlucose())
                                .pulse(patientState.getPulse())
                                .bloodPressure(patientState.getBloodPressure())
                                .temperature(patientState.getTemperature())
                                .patientName(patient.getName())
                                .emergency(patientState.isEmergency())
                                .timestamp(patientState.getTimestamp())
                                .saloonNumber(s.getNumber())
                                .build());
                    }
                });
            }
        });

        return response;
    }

    public void connectPatient(UUID patientId) {

        String stateProductionStart = createStartMessage(patientId);
        stateActionProducer.sendMessage(stateProductionStart);
    }

    public void disconnectPatient(UUID patientId) {

        String stateProductionStop = createStopMessage(patientId);
        stateActionProducer.sendMessage(stateProductionStop);
    }

    private String createStopMessage(UUID patientId) {

        Map<String, Object> message = new HashMap<>();

        message.put("patientId", patientId);
        message.put("action", "stop");

        return gson.toJson(message);
    }

    private void sendEmergencyAlert(Patient patient) {

        String emergencyMessage = "Emergency for " + patient.getName();
        emergencyProducer.sendMessage(emergencyMessage);
    }

    private boolean isEmergency(State state) {

        double temperature = state.getTemperature();

        if(temperature > 39 || temperature < 37){
            return true;
        }

        double glucose = state.getGlucose();

        if(glucose > 100){
            return true;
        }

        double bloodPressure = state.getBloodPressure();

        if(bloodPressure > 120 || bloodPressure < 90){
            return true;
        }

        double pulse = state.getPulse();

        return pulse < 60 || pulse > 100;
    }

    private State persistState(PatientStateDTO patientStateDTO) {

        State state = createState(patientStateDTO);
        state.setEmergency(isEmergency(state));

        return stateRepository.save(state);
    }

    private State createState(PatientStateDTO patientStateDTO) {

        Optional<Patient> patient = patientRepository.findById(patientStateDTO.getPatientId());

        return patient.map(value -> State
                .builder()
                .pulse(patientStateDTO.getState().getPulse())
                .glucose(patientStateDTO.getState().getGlucose())
                .bloodPressure(patientStateDTO.getState().getBloodPressure())
                .timestamp(patientStateDTO.getState().getTimestamp())
                .temperature(patientStateDTO.getState().getTemperature())
                .patient(value)
                .build()).orElse(null);
    }

    private String createStartMessage(UUID patientId) {

        Map<String, Object> message = new HashMap<>();

        message.put("patientId", patientId);
        message.put("action", "start");

        return gson.toJson(message);
    }
}