package com.stateservice.controller;

import com.stateservice.models.response.PatientStateResponse;
import com.stateservice.service.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/state")
@RestController
public class StateController {

    private final StateService stateService;

    @Autowired
    public StateController(StateService stateService){
        this.stateService = stateService;
    }


    @GetMapping("/all")
    public ResponseEntity<?> getAllPatientsState(){

        List<PatientStateResponse> patientStateResponseList = stateService.getAllPatientsState();

        if(patientStateResponseList.isEmpty()){
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body("No patients available.");
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(patientStateResponseList);
    }

    @PostMapping("/connect-patient")
    public ResponseEntity<String> connectPatient(@RequestParam("id") UUID patientId){

        stateService.connectPatient(patientId);
        return new ResponseEntity<>("Production process started successfully.", HttpStatus.OK);
    }

    @PostMapping("/disconnect-patient")
    public ResponseEntity<String> disconnectPatient(@RequestParam("id") UUID patientId){

        stateService.disconnectPatient(patientId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Successfully disconnecting patient");
    }
}
