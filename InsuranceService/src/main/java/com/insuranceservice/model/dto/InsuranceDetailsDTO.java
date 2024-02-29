package com.insuranceservice.model.dto;

import lombok.*;

import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class InsuranceDetailsDTO {

    private UUID identifier;

    private String socialNumber;
}
