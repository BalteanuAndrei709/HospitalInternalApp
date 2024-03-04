package com.identitycardservice.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.identitycardservice.kafka.consumer.PatientDetailsConsumer;
import com.identitycardservice.kafka.producer.CheckerResponseProducer;
import com.identitycardservice.models.Code;
import com.identitycardservice.models.County;
import com.identitycardservice.models.Series;
import com.identitycardservice.models.dto.PatientIdentityCardDetailsDTO;
import com.identitycardservice.models.dto.ValidationResponseDTO;
import com.identitycardservice.repository.CodeRepository;
import com.identitycardservice.repository.CountyRepository;
import com.identitycardservice.repository.SeriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class IdentityCardCheckerService {

    private final Gson gson;

    private final CodeRepository codeRepository;

    private final CountyRepository countyRepository;

    private final SeriesRepository seriesRepository;

    private final CheckerResponseProducer checkerResponseProducer;

    private static final Logger log = LoggerFactory.getLogger(PatientDetailsConsumer.class);

    @Autowired
    public IdentityCardCheckerService(Gson gson,
                                      CountyRepository countyRepository,
                                      CodeRepository codeRepository,
                                      SeriesRepository seriesRepository,
                                      CheckerResponseProducer checkerResponseProducer){
        this.gson = gson;
        this.countyRepository = countyRepository;
        this.codeRepository = codeRepository;
        this.seriesRepository = seriesRepository;
        this.checkerResponseProducer = checkerResponseProducer;
    }
    public void validateIdentityCardDetails(String value) {

        JsonObject detailsAsJson = gson.fromJson(value, JsonObject.class);

        UUID identifier = UUID.fromString(detailsAsJson.get("identifier").getAsString());
        PatientIdentityCardDetailsDTO patientDetails = constructDTO(detailsAsJson);

        boolean status = checkValidity(patientDetails);

        ValidationResponseDTO responseDTO = ValidationResponseDTO
                .builder()
                .identifier(identifier)
                .status(status)
                .build();


        String dtoAsString = gson.toJson(responseDTO);

        checkerResponseProducer.sendMessage(dtoAsString);
    }

    private boolean checkValidity(PatientIdentityCardDetailsDTO patientDetails) {

        if(!validCountyAndSeries(patientDetails.getCounty(), patientDetails.getSeries())){
            return false;
        }

        log.info("County and series match");

        if(!checkSocialNumber(patientDetails)){
            return false;
        }

        log.info("Social number matches the given details.");

        return true;
    }

    private boolean checkSocialNumber(PatientIdentityCardDetailsDTO patientDetails) {

        String socialNumber = patientDetails.getSocialNumber();
        String dateOfBirth = patientDetails.getDateOfBirth();

        String dayOfBirth = dateOfBirth.substring(0, 2);
        String monthOfBirth = dateOfBirth.substring(3, 5);
        String yearOfBirth = dateOfBirth.substring(6); // Extracts the year part

        // Validate components against the social number
        return  checkSComponent(socialNumber.charAt(0), Integer.parseInt(yearOfBirth), patientDetails.getSex()) &&
                checkAAComponent(socialNumber.substring(1, 3), yearOfBirth.substring(2)) &&
                checkLLComponent(socialNumber.substring(3, 5), monthOfBirth) &&
                checkZZComponent(socialNumber.substring(5, 7), dayOfBirth) &&
                checkJJComponent(socialNumber.substring(7, 9), patientDetails.getCounty());
    }

    private boolean checkJJComponent(String countyCode, String countyName) {

        Optional<Code> code = codeRepository.getCodeByCode(countyCode);

        if (code.isEmpty()){
            return false;
        }

        Optional<County> county = countyRepository.getCountyByName(countyName);

        return county.map(value -> value.getCode()
                .stream().anyMatch(p -> p.getId().equals(code.get().getId()))).orElse(false);

    }

    private boolean checkZZComponent(String dayFromSocialNumber, String dayFromDateOfBirth) {
        return dayFromDateOfBirth.equals(dayFromSocialNumber);
    }

    private boolean checkLLComponent(String monthFromSocialNumber, String monthFromDateOfBirth) {
        return monthFromDateOfBirth.equals(monthFromSocialNumber);
    }

    private boolean checkAAComponent(String llComponent, String lastTwoDigitsYear) {
        return llComponent.equals(lastTwoDigitsYear);
    }

    private boolean checkSComponent(Character sComponent, int yearOfBirth, String sex) {

        return switch (sComponent) {
            case '1' -> sex.equals("M") && yearOfBirth <= 1999;
            case '2' -> sex.equals("F") && yearOfBirth <= 1999;
            case '5' -> sex.equals("M") && yearOfBirth >= 2000;
            case '6' -> sex.equals("F") && yearOfBirth >= 2000;
            default -> false;
        };
    }

    private boolean validCountyAndSeries(String countyName, String seriesName) {

        Optional<Series> serie = seriesRepository.getSeriesByName(seriesName);

        if (serie.isEmpty()){
            return false;
        }

        Optional<County> county = countyRepository.getCountyByName(countyName);

        return county.map(value -> value.getSeriesList()
                .stream().anyMatch(p -> p.getId().equals(serie.get().getId()))).orElse(false);

    }

    private PatientIdentityCardDetailsDTO constructDTO(JsonObject patientIdentityCardDetails) {

        JsonObject patientDetailsObject =
                patientIdentityCardDetails.getAsJsonObject("patient_identity_card_details");

       return PatientIdentityCardDetailsDTO.builder()
                .name(patientDetailsObject.get("name").getAsString())
                .socialNumber(patientDetailsObject.get("socialNumber").getAsString())
                .county(patientDetailsObject.get("county").getAsString())
                .series(patientDetailsObject.get("series").getAsString())
                .dateOfBirth(patientDetailsObject.get("dateOfBirth").getAsString())
                .sex(patientDetailsObject.get("sex").getAsString())
                .build();
    }
}
