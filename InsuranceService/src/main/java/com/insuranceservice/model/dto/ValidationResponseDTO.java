package com.insuranceservice.model.dto;

import lombok.*;

import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class ValidationResponseDTO {

    private UUID identifier;

    private boolean status;
}
