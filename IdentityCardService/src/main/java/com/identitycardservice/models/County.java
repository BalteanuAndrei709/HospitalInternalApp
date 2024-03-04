package com.identitycardservice.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@ToString
public class County {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Code> code;

    @OneToMany(fetch = FetchType.EAGER)
    List<Series> seriesList;
}
