package com.identitycardservice.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PatientIdentityCardDetailsDTO {

    private String name;

    private String socialNumber;

    private String county;

    private String series;

    private String dateOfBirth;

    private String sex;
}
