package com.hospitalservice.models.dto;

import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString
public class ValidationResponseDTO {

    private UUID identifier;

    private Boolean status;
}
