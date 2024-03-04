package com.hospitalservice.controller;

import com.hospitalservice.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/nurse")
public class PatientController {

    private final PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService){
        this.patientService = patientService;
    }

    @PostMapping("/connect-patient")
    public ResponseEntity<String> connectPatient(@RequestParam("id") UUID patientId){

        patientService.connectPatient(patientId);
        return new ResponseEntity<>("Production process started successfully.", HttpStatus.OK);
    }

    @PostMapping("/disconnect-patient")
    public ResponseEntity<String> disconnectPatient(@RequestParam("id") UUID patientId){
        try {
            patientService.disconnectPatient(patientId);
            return new ResponseEntity<>("Patient disconnected successfully.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to disconnect patient.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
