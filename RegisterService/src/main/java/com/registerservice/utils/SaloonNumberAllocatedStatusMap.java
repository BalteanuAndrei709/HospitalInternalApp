package com.registerservice.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SaloonNumberAllocatedStatusMap {

    private final ConcurrentHashMap<UUID, CompletableFuture<Integer>> validationsStatus = new ConcurrentHashMap<>();

    public CompletableFuture<Integer> getOrCreateFuture(UUID patientDetailsId) {
        return validationsStatus.computeIfAbsent(patientDetailsId, k -> new CompletableFuture<>());
    }

    public void completeFuture(UUID patientDetailsId, Integer saloonNumber) {
        CompletableFuture<Integer> future = validationsStatus.remove(patientDetailsId);
        if (future != null) {
            future.complete(saloonNumber);
        }
    }
}
