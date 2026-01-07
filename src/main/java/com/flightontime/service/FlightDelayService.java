package com.flightontime.service;

import com.flightontime.dto.FlightDelayRequestDto;
import com.flightontime.dto.FlightDelayResponseDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class FlightDelayService {

    private static final String PYTHON_API_URL = "http://localhost:5000/predict";

    @Autowired
    private RestTemplate restTemplate;

    public FlightDelayResponseDto predictDelay(FlightDelayRequestDto requestDto) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<FlightDelayRequestDto> request =
                    new HttpEntity<>(requestDto, headers);

            ResponseEntity<FlightDelayResponseDto> response =
                    restTemplate.postForEntity(
                            PYTHON_API_URL,
                            request,
                            FlightDelayResponseDto.class
                    );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            // Errores 4xx (datos inválidos, aerolínea no soportada, etc.)
            throw new RuntimeException(
                    "Error al enviar datos al microservicio Python: " + e.getResponseBodyAsString()
            );

        } catch (HttpServerErrorException e) {
            // Errores 5xx (fallo interno del microservicio)
            throw new RuntimeException(
                    "El microservicio de predicción no está disponible"
            );
        }
    }
}
