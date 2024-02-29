package com.hospitalservice.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hospitalservice.kafka.producer.AllocatedSaloonProducer;
import com.hospitalservice.models.Patient;
import com.hospitalservice.models.Saloon;
import com.hospitalservice.models.dto.RegisterPatientDTO;
import com.hospitalservice.repository.PatientRepository;
import com.hospitalservice.repository.SaloonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PatientAllocationService {

    private final Gson gson;

    private final PatientRepository patientRepository;

    private final SaloonRepository saloonRepository;

    private final AllocatedSaloonProducer allocatedSaloonProducer;

    @Value("${kafka.topic.allocate.saloon.response}")
    private String responseTopic;

    @Autowired
    public PatientAllocationService(Gson gson,
                                    PatientRepository patientRepository,
                                    SaloonRepository saloonRepository,
                                    AllocatedSaloonProducer allocatedSaloonProducer){
        this.gson = gson;
        this.patientRepository = patientRepository;
        this.saloonRepository = saloonRepository;
        this.allocatedSaloonProducer = allocatedSaloonProducer;
    }

    public void allocateSaloon(String value) {

        RegisterPatientDTO patientDetails = createPatientDTO(value);
        UUID identifier = UUID.fromString(gson.fromJson(value, JsonObject.class).get("identifier").getAsString());

        Patient patient = persistPatient(patientDetails, identifier);


        if (patient == null){

            String errorResponseMessage = createErrorMessage("Error at persisting patient", identifier);

            sendAllocatedSaloon(errorResponseMessage);
        }
        
        Integer saloonNumber = allocateSaloonToPatient(patient);

        if(saloonNumber == -1){
            createErrorMessage("No saloon available", identifier);
        }

        String successMessage = createSuccessMessage(saloonNumber, identifier);

        sendAllocatedSaloon(successMessage);
    }

    private Integer allocateSaloonToPatient(Patient patient) {

        Saloon saloonForPatient = findSaloonForPatient(patient);

        if (saloonForPatient == null){
            return -1;
        }

        saloonForPatient.getPatientList().add(patient);
        saloonRepository.save(saloonForPatient); // Save the updated saloon

        return saloonForPatient.getNumber();
    }

    public Saloon findSaloonForPatient(Patient patient) {
        List<Saloon> saloons = saloonRepository.findAll();
        Saloon selectedSaloon = null;

        int minimumExtraFacilities = Integer.MAX_VALUE;
        int minimumPatientCount = Integer.MAX_VALUE;

        for (Saloon saloon : saloons) {
            boolean meetsNeeds = (!patient.isRespiratoryProblems() || saloon.isSpecialRespiratoryFacilities()) &&
                    (!patient.isMobilityProblems() || saloon.isSpecialMobilityFacilities()) &&
                    (!patient.isVisionProblems() || saloon.isSpecialVisionFacilities());

            if (meetsNeeds && saloon.getPatientList().size() < saloon.getCapacity()) {
                int extraFacilitiesCount = 0;
                if (patient.isRespiratoryProblems() != saloon.isSpecialRespiratoryFacilities()) extraFacilitiesCount++;
                if (patient.isMobilityProblems() != saloon.isSpecialMobilityFacilities()) extraFacilitiesCount++;
                if (patient.isVisionProblems() != saloon.isSpecialVisionFacilities()) extraFacilitiesCount++;

                if (extraFacilitiesCount < minimumExtraFacilities ||
                        (extraFacilitiesCount == minimumExtraFacilities && saloon.getPatientList().size() < minimumPatientCount)) {
                    selectedSaloon = saloon;
                    minimumExtraFacilities = extraFacilitiesCount;
                    minimumPatientCount = saloon.getPatientList().size();
                }
            }
        }

        return selectedSaloon;
    }

    private void sendAllocatedSaloon(String message) {

        allocatedSaloonProducer.sendMessage(responseTopic, message);
    }

    private String createSuccessMessage(Integer saloonNumber, UUID identifier) {

        Map<String, Object> message = new HashMap<>();

        message.put("identifier", identifier);
        message.put("problem", "none");
        message.put("saloonNumber", saloonNumber);

        return gson.toJson(message);
    }

    private String createErrorMessage(String errorAtPersistingPatient, UUID identifier) {

        Map<String, Object> message = new HashMap<>();

        message.put("identifier", identifier);
        message.put("problem", errorAtPersistingPatient);
        message.put("saloonNumber", -1);

        return gson.toJson(message);
    }

    private Patient persistPatient(RegisterPatientDTO patientDetails, UUID identifier) {

        try {
            Patient patient = Patient
                    .builder()
                    .id(identifier)
                    .name(patientDetails.getName())
                    .sex(patientDetails.getSex())
                    .series(patientDetails.getSeries())
                    .county(patientDetails.getCounty())
                    .mobilityProblems(patientDetails.isMobilityProblems())
                    .respiratoryProblems(patientDetails.isRespiratoryProblems())
                    .visionProblems(patientDetails.isVisionProblems())
                    .dateOfBirth(patientDetails.getDateOfBirth())
                    .build();

            patient = patientRepository.save(patient);

            return patient;
        }
        catch (Exception e){
            return null;
        }

    }

    private RegisterPatientDTO createPatientDTO(String value) {

        JsonObject jsonObject = new Gson().fromJson(value, JsonObject.class);
        String patientDetailsJson = jsonObject.get("patientDetails").toString();

        return gson.fromJson(patientDetailsJson, RegisterPatientDTO.class);
    }
}
