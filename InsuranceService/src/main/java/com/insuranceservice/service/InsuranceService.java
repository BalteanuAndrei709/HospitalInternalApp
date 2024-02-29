package com.insuranceservice.service;

import com.google.gson.Gson;
import com.insuranceservice.kafka.producer.CheckerResponseProducer;
import com.insuranceservice.model.dto.ValidationResponseDTO;
import com.insuranceservice.model.dto.InsuranceDetailsDTO;
import com.insuranceservice.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InsuranceService {

    private final PersonRepository personRepository;

    private final CheckerResponseProducer checkerResponseProducer;

    @Value("${kafka.topic.patient.insurance.checker.response}")
    private String insuranceResponseTopic;

    @Autowired
    public InsuranceService(PersonRepository personRepository,
                            CheckerResponseProducer checkerResponseProducer){
        this.personRepository = personRepository;
        this.checkerResponseProducer = checkerResponseProducer;
    }
    public void checkInsurance(InsuranceDetailsDTO insuranceDetails) {

        boolean patientIsInsured = personRepository.isPersonEnsured(insuranceDetails.getSocialNumber());

        ValidationResponseDTO response = ValidationResponseDTO
                .builder()
                .status(patientIsInsured)
                .identifier(insuranceDetails.getIdentifier())
                .build();

        sendResponse(response);
    }

    private void sendResponse(ValidationResponseDTO response) {
        checkerResponseProducer.sendMessage(insuranceResponseTopic, new Gson().toJson(response));
    }
}
