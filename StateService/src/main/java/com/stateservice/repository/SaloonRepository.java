package com.stateservice.repository;


import com.stateservice.models.Saloon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaloonRepository extends JpaRepository<Saloon, Integer> {

}
