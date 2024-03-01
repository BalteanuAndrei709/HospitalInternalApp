package com.hospitalservice.models.response;

import com.hospitalservice.models.Department;
import com.hospitalservice.models.Patient;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllPatientInSaloonsResponse {

    private Integer saloonNumber;

    private Department department;

    private List<Patient> patientsInSaloons;
}
