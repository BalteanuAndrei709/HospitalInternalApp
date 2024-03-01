package com.hospitalservice.repository;

import com.hospitalservice.models.Saloon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaloonRepository extends JpaRepository<Saloon, Integer> {

    List<Saloon> findAllByPatientListIsNotEmpty();
}
