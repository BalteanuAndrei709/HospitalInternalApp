package com.stateproductionservice.repository;

import com.stateproductionservice.models.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActionRepository extends JpaRepository<Action, UUID> {

    Action getActionByPatientIdOrderByTimestampDesc(UUID patientId);

    Action getTopByPatientIdOrderByTimestampDesc(UUID patientId);
}
