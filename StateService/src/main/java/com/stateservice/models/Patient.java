package com.stateservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    private UUID id;

    private String name;

    private String county;

    private String series;

    private String dateOfBirth;

    private String sex;

    @Column(unique = true)
    private String socialNumber;

    private boolean respiratoryProblems;

    private boolean mobilityProblems;

    private boolean visionProblems;
}
