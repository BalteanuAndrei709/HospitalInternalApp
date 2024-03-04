package com.identitycardservice.repository;

import com.identitycardservice.models.County;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CountyRepository extends JpaRepository<County, UUID> {

    Optional<County> getCountyByName(String name);

}
