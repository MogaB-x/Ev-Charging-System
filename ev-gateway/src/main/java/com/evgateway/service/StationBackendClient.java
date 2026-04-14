package com.evgateway.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class StationBackendClient {
    private static final Logger log = LoggerFactory.getLogger(StationBackendClient.class);

    private final RestTemplate restTemplate;

    public StationBackendClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean stationExists(String stationIdentity) {
        String url = "http://localhost:8080/internal/stations/by-ocpp-identity/" + stationIdentity;

        try {
            Object response = restTemplate.getForObject(url, Object.class);

            log.info("Backend response OK for station={}", stationIdentity);
            return response != null;

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Station NOT FOUND in backend: {}", stationIdentity);
            return false;

        } catch (Exception e) {
            log.error("Error calling backend for station={}", stationIdentity, e);
            return false;
        }
    }
}
