package com.identitycardservice.repository;

import com.identitycardservice.models.Series;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SeriesRepository extends JpaRepository<Series, UUID> {

    Optional<Series> getSeriesByName(String name);
}
