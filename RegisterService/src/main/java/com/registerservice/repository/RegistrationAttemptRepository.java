package com.registerservice.repository;

import com.registerservice.model.RegistrationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RegistrationAttemptRepository extends JpaRepository<RegistrationAttempt, UUID> {

    boolean existsById(UUID id);
}
