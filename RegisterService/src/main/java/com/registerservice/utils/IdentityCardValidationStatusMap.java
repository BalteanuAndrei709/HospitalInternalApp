package com.registerservice.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public final class IdentityCardValidationStatusMap {

    private final ConcurrentHashMap<UUID, CompletableFuture<Boolean>> validationsStatus = new ConcurrentHashMap<>();

    public CompletableFuture<Boolean> getOrCreateFuture(UUID patientDetailsId) {
        return validationsStatus.computeIfAbsent(patientDetailsId, k -> new CompletableFuture<>());
    }

    public void completeFuture(UUID patientDetailsId, boolean isValid) {
        CompletableFuture<Boolean> future = validationsStatus.remove(patientDetailsId);
        if (future != null) {
            future.complete(isValid);
        }
    }
}
