package com.hospitalservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

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

    private boolean respiratoryProblems;

    private boolean mobilityProblems;

    private boolean visionProblems;
}