package com.registerservice.model.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class PatientIdentityCardDetailsDTO {

    private String name;

    private String socialNumber;

    private String county;

    private String series;

    private String dateOfBirth;

    private char sex;
}
