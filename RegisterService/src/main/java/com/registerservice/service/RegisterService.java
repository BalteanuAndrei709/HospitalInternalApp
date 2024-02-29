package com.registerservice.service;

import com.google.gson.Gson;
import com.registerservice.kafka.producer.PatientDetailsCheckerProducer;
import com.registerservice.kafka.producer.PatientInsuranceCheckerProducer;
import com.registerservice.kafka.producer.PatientPersistProducer;
import com.registerservice.model.RegistrationAttempt;
import com.registerservice.model.dto.PatientIdentityCardDetailsDTO;
import com.registerservice.model.dto.RegisterPatientDTO;
import com.registerservice.repository.RegistrationAttemptRepository;
import com.registerservice.utils.InsuranceValidationStatusMap;
import com.registerservice.utils.SaloonNumberAllocatedStatusMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import com.registerservice.utils.IdentityCardValidationStatusMap;
@Service
public class RegisterService {


    @Value("${kafka.topic.patient.details.checker}")
    private String patientIdentityCardCheckerTopic;

    @Value("${kafka.topic.patient.insurance.checker}")
    private String patientInsuranceCheckerTopic;

    @Value("${kafka.topic.allocate.saloon}")
    private String allocateSaloonTopic;
    
    private final PatientDetailsCheckerProducer patientDetailsCheckerProducer;

    private final PatientInsuranceCheckerProducer patientInsuranceCheckerProducer;

    private final PatientPersistProducer patientPersistProducer;

    private final RegistrationAttemptRepository registrationAttemptRepository;

    private final IdentityCardValidationStatusMap identityCardValidationStatusMap;

    private final InsuranceValidationStatusMap insuranceValidationStatusMap;

    private final SaloonNumberAllocatedStatusMap saloonNumberAllocatedStatusMap;


    private final Gson gson;

    private static final Logger log = LoggerFactory.getLogger(RegisterService.class);

    @Autowired
    public RegisterService(PatientDetailsCheckerProducer patientDetailsCheckerProducer,
                           PatientInsuranceCheckerProducer patientInsuranceCheckerProducer,
                           PatientPersistProducer patientPersistProducer,
                           RegistrationAttemptRepository registrationAttemptRepository,
                           Gson gson,
                           IdentityCardValidationStatusMap identityCardValidationStatusMap,
                           InsuranceValidationStatusMap insuranceValidationStatusMap,
                           SaloonNumberAllocatedStatusMap saloonNumberAllocatedStatusMap){
        this.patientDetailsCheckerProducer = patientDetailsCheckerProducer;
        this.patientInsuranceCheckerProducer = patientInsuranceCheckerProducer;
        this.patientPersistProducer = patientPersistProducer;
        this.registrationAttemptRepository = registrationAttemptRepository;
        this.gson = gson;
        this.identityCardValidationStatusMap = identityCardValidationStatusMap;
        this.insuranceValidationStatusMap = insuranceValidationStatusMap;
        this.saloonNumberAllocatedStatusMap = saloonNumberAllocatedStatusMap;
    }

    public Integer registerPatient(RegisterPatientDTO registerPatientDTO) {

        PatientIdentityCardDetailsDTO patientIdentityCardDetailsDTO = extractIdentityCardDetails(registerPatientDTO);

        // save the registration attempt
        UUID identifier = getRandomNotUsedIdentifier();
        RegistrationAttempt registrationAttempt = registrationAttemptRepository.save(
                    RegistrationAttempt
                            .builder()
                            .id(identifier)
                            .build());

        // check the patient identity card details
        boolean validPatientDetails = checkPatientDetails(patientIdentityCardDetailsDTO,identifier);

        if(!validPatientDetails){
            return -1;
        }

        // check the patient has a medical insurance
         boolean patientHasInsurance = checkPatientInsurance(patientIdentityCardDetailsDTO.getSocialNumber(), identifier);

        if(!patientHasInsurance){
            return -1;
        }


        registrationAttempt.setSuccess(true);
        registrationAttemptRepository.save(registrationAttempt);

        return allocateSaloon(registerPatientDTO, identifier);
    }

    private Integer allocateSaloon(RegisterPatientDTO registerPatientDTO, UUID identifier) {

        String message = createPersistPatientMessage(registerPatientDTO, identifier);
        patientPersistProducer.sendMessage(allocateSaloonTopic, message);

        return waitForSaloonNumber(identifier);
    }

    private Integer waitForSaloonNumber(UUID identifier) {

        CompletableFuture<Integer> future = saloonNumberAllocatedStatusMap.getOrCreateFuture(identifier);

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

    private String createPersistPatientMessage(RegisterPatientDTO registerPatientDTO, UUID identifier) {

        Map<String, Object> message = new HashMap<>();

        message.put("identifier", identifier);
        message.put("patientDetails", registerPatientDTO);

        return gson.toJson(message);
    }

    private boolean checkPatientInsurance(String socialNumber, UUID identifier) {

        String message = createInsuranceCheckMessage(identifier,socialNumber);
        log.info("Checking the insurance for patient with these details : " + message);
        patientInsuranceCheckerProducer.sendMessage(patientInsuranceCheckerTopic, message);

        return waitForInsuranceValidationResult(identifier);
    }

    private boolean waitForInsuranceValidationResult(UUID identifier) {

        CompletableFuture<Boolean> future = insuranceValidationStatusMap.getOrCreateFuture(identifier);

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

    private String createInsuranceCheckMessage(UUID identifier, String socialNumber) {

        Map<String, Object> messageMap = new HashMap<>();

        messageMap.put("identifier", identifier.toString());
        messageMap.put("socialNumber", socialNumber);

        return gson.toJson(messageMap);
    }

    private PatientIdentityCardDetailsDTO extractIdentityCardDetails(RegisterPatientDTO registerPatientDTO) {
        return PatientIdentityCardDetailsDTO
                .builder()
                .dateOfBirth(registerPatientDTO.getDateOfBirth())
                .sex(registerPatientDTO.getSex())
                .county(registerPatientDTO.getCounty())
                .series(registerPatientDTO.getSeries())
                .socialNumber(registerPatientDTO.getSocialNumber())
                .name(registerPatientDTO.getName())
                .build();
    }

    private boolean checkPatientDetails(PatientIdentityCardDetailsDTO patientIdentityCardDetailsDTO, UUID identifier) {

        String message = createIdentityCardCheckMessage(identifier, patientIdentityCardDetailsDTO);
        log.info(message);
        patientDetailsCheckerProducer.sendMessage(patientIdentityCardCheckerTopic, message);

        return waitForIdentityCardValidationResult(identifier);
    }

    private String createIdentityCardCheckMessage(UUID identifier, PatientIdentityCardDetailsDTO patientIdentityCardDetailsDTO) {

        Map<String, Object> messageMap = new HashMap<>();

        messageMap.put("identifier", identifier.toString());
        messageMap.put("patient_identity_card_details", patientIdentityCardDetailsDTO);

        return gson.toJson(messageMap);
    }

    private UUID getRandomNotUsedIdentifier() {

        while(true){
            UUID randomUUID = UUID.randomUUID();

            if(!registrationAttemptRepository.existsById(randomUUID)){
                return randomUUID;
            }
        }
    }

    private boolean waitForIdentityCardValidationResult(UUID identifier) {

        CompletableFuture<Boolean> future = identityCardValidationStatusMap.getOrCreateFuture(identifier);

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
