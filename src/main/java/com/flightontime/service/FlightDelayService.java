package com.flightontime.service;

import com.flightontime.dto.FlightDelayModelRequestDto;
import com.flightontime.dto.FlightDelayRequestDto;
import com.flightontime.dto.FlightDelayResponseDto;
import com.flightontime.exception.AerolineaNoEncontradaException;
import com.flightontime.exception.ExternalServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;

@Service
public class FlightDelayService {

    private static final String PYTHON_API_URL = "http://localhost:5000/predict";

    @Autowired
    private RestTemplate restTemplate;

    public FlightDelayResponseDto predictDelay(FlightDelayRequestDto requestDto) {

        try {

            // 1️⃣ Calcular día de la semana y finde
            DayOfWeek dayOfWeek = requestDto.getFecha_salida().getDayOfWeek();

            int diaSemana = dayOfWeek.getValue(); // 1=Lunes ... 7=Domingo
            int esFinde = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) ? 1 : 0;

            // 2️⃣ Armar DTO que espera el modelo
            FlightDelayModelRequestDto modelDto = new FlightDelayModelRequestDto();
            modelDto.setAerolinea(requestDto.getAerolinea());
            modelDto.setHora_salida(requestDto.getHora_salida());
            modelDto.setDia_semana(diaSemana);
            modelDto.setEs_finde(esFinde);
            modelDto.setDistancia_km(requestDto.getDistancia_km());
            modelDto.setTaxi_out(requestDto.getTaxi_out());



            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            /*HttpEntity<FlightDelayRequestDto> request =
                    new HttpEntity<>(requestDto, headers);*/

            HttpEntity<FlightDelayModelRequestDto> request =
                    new HttpEntity<>(modelDto, headers);

            ResponseEntity<FlightDelayResponseDto> response =
                    restTemplate.postForEntity(
                            PYTHON_API_URL,
                            request,
                            FlightDelayResponseDto.class
                    );

            return response.getBody();

        } catch (HttpClientErrorException.BadRequest e) {
            // Error 400 desde FastAPI (ej: aerolínea inexistente)
            throw new AerolineaNoEncontradaException(
                    extraerMensaje(e)
            );

        } catch (HttpServerErrorException e) {
            // Errores 5xx (fallo interno del microservicio)
            throw new ExternalServiceException(
                    "Error al comunicarse con el microservicio de predicción"
            );
        }
        
        
    }

    private String extraerMensaje(HttpClientErrorException.BadRequest e) {
        try {
            String body = e.getResponseBodyAsString();

            if (body == null || body.isBlank()) {
                return "Aerolínea no soportada";
            }

            // Extrae {"detail":"mensaje"}
            int start = body.indexOf("\"detail\":\"");
            if (start != -1) {
                start += 10;
                int end = body.indexOf("\"", start);
                if (end != -1) {
                    return body.substring(start, end);
                }
            }

            return body;

        } catch (Exception ex) {
            return "Aerolínea no soportada";
        }
    }
}
