package com.hospitalservice.models.dto;


import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RegisterPatientDTO {

    private String name;

    private String socialNumber;

    private String county;

    private String series;

    private String dateOfBirth;

    private String sex;

    private String department;

    private boolean respiratoryProblems;

    private boolean mobilityProblems;

    private boolean visionProblems;
}
