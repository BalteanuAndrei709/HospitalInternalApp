package com.registerservice.service;

import com.google.gson.Gson;
import com.registerservice.kafka.producer.PatientDetailsCheckerProducer;
import com.registerservice.kafka.producer.PatientInsuranceCheckerProducer;
import com.registerservice.kafka.producer.AllocateSaloonCheckerProducer;
import com.registerservice.model.RegistrationAttempt;
import com.registerservice.model.dto.PatientIdentityCardDetailsDTO;
import com.registerservice.model.dto.RegisterPatientDTO;
import com.registerservice.repository.RegistrationAttemptRepository;
import com.registerservice.utils.IdentityCardValidationStatusMap;
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
@Service
public class RegisterService {
    
    private final PatientDetailsCheckerProducer patientDetailsCheckerProducer;

    private final PatientInsuranceCheckerProducer patientInsuranceCheckerProducer;

    private final AllocateSaloonCheckerProducer allocateSaloonCheckerProducer;

    private final RegistrationAttemptRepository registrationAttemptRepository;

    private final IdentityCardValidationStatusMap identityCardValidationStatusMap;

    private final InsuranceValidationStatusMap insuranceValidationStatusMap;

    private final SaloonNumberAllocatedStatusMap saloonNumberAllocatedStatusMap;

    private final Gson gson;

    private static final Logger log = LoggerFactory.getLogger(RegisterService.class);

    @Autowired
    public RegisterService(PatientDetailsCheckerProducer patientDetailsCheckerProducer,
                           PatientInsuranceCheckerProducer patientInsuranceCheckerProducer,
                           AllocateSaloonCheckerProducer allocateSaloonCheckerProducer,
                           RegistrationAttemptRepository registrationAttemptRepository,
                           Gson gson,
                           IdentityCardValidationStatusMap identityCardValidationStatusMap,
                           InsuranceValidationStatusMap insuranceValidationStatusMap,
                           SaloonNumberAllocatedStatusMap saloonNumberAllocatedStatusMap){
        this.patientDetailsCheckerProducer = patientDetailsCheckerProducer;
        this.patientInsuranceCheckerProducer = patientInsuranceCheckerProducer;
        this.allocateSaloonCheckerProducer = allocateSaloonCheckerProducer;
        this.registrationAttemptRepository = registrationAttemptRepository;
        this.gson = gson;
        this.identityCardValidationStatusMap = identityCardValidationStatusMap;
        this.insuranceValidationStatusMap = insuranceValidationStatusMap;
        this.saloonNumberAllocatedStatusMap = saloonNumberAllocatedStatusMap;
    }

    /*
     Method responsible for registering the patient.
     */
    public Integer registerPatient(RegisterPatientDTO registerPatientDTO) {

        // extract the patient identity card details from the request input
        PatientIdentityCardDetailsDTO patientIdentityCardDetailsDTO = extractIdentityCardDetails(registerPatientDTO);

        // save the registration attempt
        UUID identifier = getRandomNotUsedIdentifier();
        RegistrationAttempt registrationAttempt = registrationAttemptRepository.save(
                    RegistrationAttempt
                            .builder()
                            .id(identifier)
                            .build());

        try {

            // Initiate both checks asynchronously
            CompletableFuture<Boolean> validPatientDetailsFuture = checkPatientDetails(
                    patientIdentityCardDetailsDTO,
                    identifier);
            CompletableFuture<Boolean> patientHasInsuranceFuture = checkPatientInsurance(
                    patientIdentityCardDetailsDTO.getSocialNumber(),
                    identifier);
            CompletableFuture<Integer> allocatedSaloonFuture = allocateSaloon(registerPatientDTO, identifier);

            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(
                    validPatientDetailsFuture,
                    patientHasInsuranceFuture);

            combinedFuture.get();

            // Now that all three futures have completed, check their results
            boolean validPatientDetails = validPatientDetailsFuture.get();
            boolean patientHasInsurance = patientHasInsuranceFuture.get();


            // in case of a failed checks or no available saloon, return -1
            if (!validPatientDetails || !patientHasInsurance) {
                return -1;
            }

            int allocatedSaloon = allocatedSaloonFuture.get();

            if(allocatedSaloon == -1){
                return -1;
            }

            // checks pass
            registrationAttempt.setSuccess(true);
            registrationAttemptRepository.save(registrationAttempt);

            return allocatedSaloon;

        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
            return -1;
        }
    }


    private CompletableFuture<Integer> allocateSaloon(RegisterPatientDTO registerPatientDTO, UUID identifier) {

        CompletableFuture<Integer> futureResult =  saloonNumberAllocatedStatusMap.getOrCreateFuture(identifier);

        String message = createPersistPatientMessage(registerPatientDTO, identifier);

        allocateSaloonCheckerProducer.sendMessage(message);

        return futureResult;
    }

    private String createPersistPatientMessage(RegisterPatientDTO registerPatientDTO, UUID identifier) {

        Map<String, Object> message = new HashMap<>();

        message.put("identifier", identifier);
        message.put("patientDetails", registerPatientDTO);

        return gson.toJson(message);
    }

    private CompletableFuture<Boolean> checkPatientInsurance(String socialNumber, UUID identifier) {

        CompletableFuture<Boolean> futureResult = insuranceValidationStatusMap.getOrCreateFuture(identifier);

        String message = createInsuranceCheckMessage(identifier,socialNumber);

        patientInsuranceCheckerProducer.sendMessage(message);

        return futureResult;
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

    private CompletableFuture<Boolean> checkPatientDetails(PatientIdentityCardDetailsDTO patientIdentityCardDetailsDTO, UUID identifier){

        CompletableFuture<Boolean> futureResult = identityCardValidationStatusMap.getOrCreateFuture(identifier);

        String message = createIdentityCardCheckMessage(identifier, patientIdentityCardDetailsDTO);

        patientDetailsCheckerProducer.sendMessage(message);

        return futureResult;
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
}
