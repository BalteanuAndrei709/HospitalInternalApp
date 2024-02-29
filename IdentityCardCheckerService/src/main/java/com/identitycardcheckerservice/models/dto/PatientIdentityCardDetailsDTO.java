package com.identitycardcheckerservice.models.dto;

import lombok.*;

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
