package com.stateservice.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stateservice.kafka.consumer.PatientStateConsumer;
import com.stateservice.kafka.producer.EmergencyProducer;
import com.stateservice.kafka.producer.EnvironmentStateValidationProducer;
import com.stateservice.models.Patient;
import com.stateservice.models.State;
import com.stateservice.models.dto.PatientStateDTO;
import com.stateservice.repository.PatientRepository;
import com.stateservice.repository.StateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StateService {

    private static final Logger log = LoggerFactory.getLogger(StateService.class);

    private final StateRepository stateRepository;

    private final PatientRepository patientRepository;

    private final EmergencyProducer emergencyProducer;
    @Autowired
    public StateService(PatientRepository patientRepository,
                        StateRepository stateRepository,
                        EmergencyProducer emergencyProducer) {
        this.patientRepository = patientRepository;
        this.stateRepository = stateRepository;
        this.emergencyProducer = emergencyProducer;
    }

    public void processState(String value) {

        Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy, hh:mm:ss a").create();
        PatientStateDTO patientStateDTO = gson.fromJson(value, PatientStateDTO.class);

        State state = persistState(patientStateDTO);
        
        if(state == null){
            return;
        }
        
        boolean emergency = isEmergency(state);

        if(emergency){
            sendEmergencyAlert(state.getPatient());
        }
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

        return state == null ? null : stateRepository.save(state);
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
}
