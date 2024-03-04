package com.insuranceservice.service;

import com.google.gson.Gson;
import com.insuranceservice.kafka.producer.PatientInsuranceCheckResponse;
import com.insuranceservice.model.dto.ValidationResponseDTO;
import com.insuranceservice.model.dto.InsuranceDetailsDTO;
import com.insuranceservice.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InsuranceService {

    private final PersonRepository personRepository;

    private final PatientInsuranceCheckResponse patientInsuranceCheckResponse;

    @Autowired
    public InsuranceService(PersonRepository personRepository,
                            PatientInsuranceCheckResponse patientInsuranceCheckResponse){
        this.personRepository = personRepository;
        this.patientInsuranceCheckResponse = patientInsuranceCheckResponse;
    }

    /*
     Check if the citizen is insured or not
     */
    public void checkInsurance(InsuranceDetailsDTO insuranceDetails) {

        Boolean patientIsInsured = personRepository.isPersonEnsured(insuranceDetails.getSocialNumber());

        ValidationResponseDTO response = ValidationResponseDTO
                .builder()
                .status(patientIsInsured != null)
                .identifier(insuranceDetails.getIdentifier())
                .build();

        sendResponse(response);
    }

    private void sendResponse(ValidationResponseDTO response) {
        patientInsuranceCheckResponse.sendMessage(new Gson().toJson(response));
    }
}
