package com.hospitalservice.utils;

import com.google.gson.Gson;
import com.hospitalservice.models.dto.StateDetailsDTO;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

@Component
public class RandomStateGenerator {

    private final Random random = new Random();

    public StateDetailsDTO getRandomDetails() {
        return StateDetailsDTO
                .builder()
                .glucose(generateRandomGlucose())
                .temperature(generateRandomTemperature())
                .pulse(generateRandomPulse())
                .bloodPressure(generateBloodPressure())
                .timestamp(Date.from(Instant.now()))
                .build();
    }

    private double generateRandomGlucose(){
        return random.nextDouble() < 0.9 ? 100 - 10 * random.nextDouble() : 100 + 10 * random.nextDouble();
    }

    private int generateRandomPulse() {
        double chance = random.nextDouble();
        if (chance < 0.9) {
            return (int) (60 + chance * (100 - 60));
        }
        else{
            return (int) (chance < 0.95 ? 60 - random.nextDouble() * 10 :  (100 + random.nextDouble() * 10));
        }
    }

    private double generateBloodPressure() {
        double chance = random.nextDouble();
        if (chance < 0.9) {
            return 90 + random.nextDouble() * (120 - 90);
        }
        else{
            return chance < 0.95 ? 90 - chance * 19.0 : 120 + chance * 10.0;
        }
    }

    private double generateRandomTemperature() {
        double chance = random.nextDouble();

        if (chance < 0.9) {
            return 37.5 + chance;
        } else {
            return chance < 0.95 ? 37.5 - chance * 3.0 : 38.5 + chance * 3.0;
        }
    }
}
