package com.stateservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Saloon {

    @Id
    private Integer number;

    @Enumerated(EnumType.STRING)
    private Department department;

    @OneToMany(fetch = FetchType.EAGER)
    List<Patient> patientList;

    private Integer capacity;

    private boolean specialRespiratoryFacilities;

    private boolean specialMobilityFacilities;

    private boolean specialVisionFacilities;
}
