package com.saifurtech.locale.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saifurtech.locale.dao.entity.Location;
import com.saifurtech.locale.dao.entity.ZipCodesByCountryCityAndState;
import com.saifurtech.locale.dto.PostalCodes;
import com.saifurtech.locale.repo.LocaleRepo;
import com.saifurtech.locale.util.ZipcodeBaseRestClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class LocationService {

    @Autowired
    private LocaleRepo localeRepo;

    @Autowired
    private ZipcodeBaseRestClient zipcodeBaseRestClient;

    @Value("${zipcodebase.url}")
    private String zipCodeBaseUrl;


    public List<Location> locationByZipCode(String zipCode) {
        Iterable<Location> allById = localeRepo.findAllByZipCode(zipCode);
        if (!allById.iterator().hasNext()) {
            return getLocationsFromZipCodeBase(zipCode);
        } else {
            List<Location> locations = new ArrayList<>();
            allById.forEach(locations::add);
            return locations;
        }

    }

    private List<Location> getLocationsFromZipCodeBase(String zipCode) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("codes", zipCode);
        ResponseEntity<?> locationResponseEntity = zipcodeBaseRestClient.get(zipCodeBaseUrl + "/search", params, new String());
        String body = Objects.requireNonNull(locationResponseEntity.getBody()).toString();
        List<Location> locations;
        try {
            JSONObject parent = new JSONObject(body);
            Object results = parent.get("results");
            JSONObject child = new JSONObject(results.toString());
            Object locationsJson = child.get(zipCode);
            ObjectMapper objectMapper = new ObjectMapper();
            locations = objectMapper.readValue(locationsJson.toString(), new TypeReference<>() {
            });
        } catch (JSONException | IOException e) {
            throw new RuntimeException(e);
        }
        saveLocationIfDoesNotExist(locations);
        return locations;
    }

    public ZipCodesByCountryCityAndState getPostalCodes(String country, String state, String city) {
        ZipCodesByCountryCityAndState allById = localeRepo
                .findAllZipByCountryStateAndCity(country, state, city);
        if (allById == null) {
            PostalCodes postalCodes = getZipcodesByCountryStateAndCity(country, state, city);
            return savePostalCodesToDB(postalCodes);
        } else {
            return allById;
        }
    }


    private PostalCodes getZipcodesByCountryStateAndCity(String country, String state, String city) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("city", city);
        params.put("state", state);
        params.put("country", country);
        ResponseEntity<?> locationResponseEntity = zipcodeBaseRestClient.get(zipCodeBaseUrl + "/code/city",
                params, new PostalCodes());

        return (PostalCodes) locationResponseEntity.getBody();
    }

    public void saveLocationIfDoesNotExist(List<Location> locations) {
        locations.forEach(location -> {
            localeRepo.save(location);
        });
    }

    private ZipCodesByCountryCityAndState savePostalCodesToDB(PostalCodes postalCodes) {
        ZipCodesByCountryCityAndState entity = mapToZipcodeEntity(postalCodes);
        localeRepo.save(entity);
        return entity;
    }

    private ZipCodesByCountryCityAndState mapToZipcodeEntity(PostalCodes postalCodes) {
        ZipCodesByCountryCityAndState zipCodesByCountryCityAndState = new ZipCodesByCountryCityAndState();
        zipCodesByCountryCityAndState.setState(postalCodes.getQuery().getState());
        zipCodesByCountryCityAndState.setCity(postalCodes.getQuery().getCity());
        zipCodesByCountryCityAndState.setCountry(postalCodes.getQuery().getCountry());
        zipCodesByCountryCityAndState.setAllZipCodes(postalCodes.getResults());
        return zipCodesByCountryCityAndState;
    }
}
