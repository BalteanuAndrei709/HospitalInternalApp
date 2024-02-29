package com.hospitalservice.repository;

import com.hospitalservice.models.Saloon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaloonRepository extends JpaRepository<Saloon, Integer> {
}
