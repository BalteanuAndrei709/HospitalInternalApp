package com.hospitalservice.service;

import com.hospitalservice.models.Saloon;
import com.hospitalservice.models.response.AllPatientInSaloonsResponse;
import com.hospitalservice.repository.SaloonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaloonService {

    private final SaloonRepository saloonRepository;

    @Autowired
    public SaloonService(SaloonRepository saloonRepository){
        this.saloonRepository = saloonRepository;
    }

    public List<AllPatientInSaloonsResponse> getPatientsFromSaloons() {
        return saloonRepository.findAllByPatientListIsNotEmpty()
                .stream()
                .map(s -> new AllPatientInSaloonsResponse(s.getNumber(), s.getDepartment(), s.getPatientList()))
                .collect(Collectors.toList());
    }
}
