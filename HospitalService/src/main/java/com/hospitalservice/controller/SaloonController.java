package com.hospitalservice.controller;

import com.hospitalservice.models.Saloon;
import com.hospitalservice.models.response.AllPatientInSaloonsResponse;
import com.hospitalservice.service.SaloonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RequestMapping("/api/saloon")
@RestController
public class SaloonController {

    private final SaloonService saloonService;

    @Autowired
    public SaloonController(SaloonService saloonService){
        this.saloonService = saloonService;
    }

    @GetMapping("/all-patients")
    public ResponseEntity<?> getAllPatients(){

        List<AllPatientInSaloonsResponse> patientsInSaloons = saloonService.getPatientsFromSaloons();

        if(patientsInSaloons.isEmpty()){
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body("No patients available.");
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(patientsInSaloons);
    }
}
