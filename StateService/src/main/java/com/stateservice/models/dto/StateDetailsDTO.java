package com.stateservice.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StateDetailsDTO {

    private Double temperature;

    private Double bloodPressure;

    private Integer pulse;

    private Double glucose;

    private Date timestamp;
}
