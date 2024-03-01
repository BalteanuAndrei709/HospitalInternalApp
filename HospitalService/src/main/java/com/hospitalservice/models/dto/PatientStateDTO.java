package com.hospitalservice.models.dto;


import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString
@Builder
public class PatientStateDTO {

    private UUID patientId;

    private StateDetailsDTO state;
}
