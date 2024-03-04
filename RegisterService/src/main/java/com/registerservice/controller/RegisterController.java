package com.registerservice.controller;

import com.registerservice.model.dto.RegisterPatientDTO;
import com.registerservice.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registration")
public class RegisterController {

    private final RegisterService registerService;

    @Autowired
    public RegisterController(RegisterService registerService){
        this.registerService = registerService;
    }

    /*
     API responsible for registration of a patient.
     Returns the allocated saloon number, -1 in case of error or no available saloon.
     */
    @PostMapping
    public ResponseEntity<?> registerPatient(@RequestBody RegisterPatientDTO patientDetails){

        Integer allocatedSaloon = registerService.registerPatient(patientDetails);

        return ResponseEntity
                .status(allocatedSaloon == -1 ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK)
                .body(allocatedSaloon);
    }
}
