package com.identitycardservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.UUID;
@Entity
@Data
public class Series {

    @Id
    private UUID id;

    private String name;
}
