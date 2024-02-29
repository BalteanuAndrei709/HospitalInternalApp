package com.identitycardcheckerservice.repository;

import com.identitycardcheckerservice.models.County;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CountyRepository extends JpaRepository<County, UUID> {

    Optional<County> getCountyByName(String name);

}
