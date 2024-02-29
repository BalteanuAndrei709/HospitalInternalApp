package com.insuranceservice.repository;

import com.insuranceservice.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PersonRepository extends JpaRepository<Person, UUID> {

    @Query(value = "select p.insured from person p where p.social_number = ?1", nativeQuery = true)
    Boolean  isPersonEnsured(String socialNumber);
}
