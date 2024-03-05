package com.stateservice.models.response;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@Builder
public class PatientStateResponse {

    private Integer saloonNumber;
    private String patientName;

    private Double temperature;

    private Double bloodPressure;

    private Integer pulse;

    private Double glucose;

    private Date timestamp;

    private boolean emergency;
}
