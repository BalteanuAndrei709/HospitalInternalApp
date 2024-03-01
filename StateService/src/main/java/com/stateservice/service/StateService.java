package com.stateservice.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.stateservice.kafka.producer.EnvironmentStateValidationProducer;
import com.stateservice.models.dto.PatientStateDTO;
import com.stateservice.models.dto.StateDetailsDTO;
import com.stateservice.models.dto.ValidationResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StateService {
    private static final Logger log = LoggerFactory.getLogger(StateService.class);

    private final Gson gson = new Gson();

    private final JdbcTemplate jdbcTemplate;

    private final EnvironmentStateValidationProducer environmentStateValidationProducer;

    @Autowired
    public StateService(JdbcTemplate jdbcTemplate,
                        EnvironmentStateValidationProducer environmentStateValidationProducer) {
        this.jdbcTemplate = jdbcTemplate;
        this.environmentStateValidationProducer = environmentStateValidationProducer;
    }

    public void createEnvironmentStateForPatient(String value) {

        UUID patientId = UUID.fromString(gson.fromJson(value, String.class));

        boolean createdTable = createTableForPatientState(patientId);

        sendResponse(createdTable, patientId);
    }

    private void sendResponse(boolean createdTable, UUID patientId) {

        ValidationResponseDTO validationResponseDTO = ValidationResponseDTO
                .builder()
                .identifier(patientId)
                .status(createdTable)
                .build();

        environmentStateValidationProducer.sendMessage(gson.toJson(validationResponseDTO));
    }

    private boolean createTableForPatientState(UUID patientId) {
        String tableName = "patient_state" + patientId.toString().replace("-", "_");
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "temperature DOUBLE PRECISION, " +
                "blood_pressure DOUBLE PRECISION, " +
                "pulse INTEGER, " +
                "glucose DOUBLE PRECISION, " +
                "patientId uuid, " +
                "timestamp TIMESTAMP WITHOUT TIME ZONE PRIMARY KEY" +
                ");";
        try {
            jdbcTemplate.execute(sql);
            return true; // Table created successfully
        } catch (Exception e) {
            log.error(e.getMessage());
            return false; // Table creation failed
        }
    }

    public void processState(String value) {

        Gson gson = new GsonBuilder().setDateFormat("MMM d, yyyy, hh:mm:ss a").create();
        PatientStateDTO patientState = gson.fromJson(value, PatientStateDTO.class);
        persistState(patientState);
    }

    private boolean persistState(PatientStateDTO patientState) {

        String tableName = "patient_state" + patientState.getPatientId().toString().replace("-", "_");
        String sql = "INSERT INTO " + tableName + " (temperature, blood_pressure, pulse, glucose, patientId, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql,
                    patientState.getState().getTemperature(),
                    patientState.getState().getBloodPressure(),
                    patientState.getState().getPulse(),
                    patientState.getState().getGlucose(),
                    patientState.getPatientId(),
                    patientState.getState().getTimestamp());
            return true; // Data inserted successfully
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Insertion failed
        }
    }

}
