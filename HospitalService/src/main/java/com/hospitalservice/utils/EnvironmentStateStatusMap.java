package com.hospitalservice.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EnvironmentStateStatusMap {

    private final ConcurrentHashMap<UUID, CompletableFuture<Boolean>> validationsStatus = new ConcurrentHashMap<>();

    public CompletableFuture<Boolean> getOrCreateFuture(UUID patientId) {
        return validationsStatus.computeIfAbsent(patientId, k -> new CompletableFuture<>());
    }

    public void completeFuture(UUID patientId, Boolean status) {
        CompletableFuture<Boolean> future = validationsStatus.remove(patientId);
        if (future != null) {
            future.complete(status);
        }
    }
}
