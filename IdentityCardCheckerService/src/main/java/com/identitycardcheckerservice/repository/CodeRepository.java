package com.identitycardcheckerservice.repository;

import com.identitycardcheckerservice.models.Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodeRepository extends JpaRepository<Code, UUID> {

    Optional<Code> getCodeByCode(String countyCode);

}
