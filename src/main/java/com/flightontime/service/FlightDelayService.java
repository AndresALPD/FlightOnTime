package com.flightontime.service;

import com.flightontime.dto.FlightDelayModelRequestDto;
import com.flightontime.dto.FlightDelayRequestDto;
import com.flightontime.dto.FlightDelayResponseDto;
import com.flightontime.exception.AerolineaNoEncontradaException;
import com.flightontime.exception.ExternalServiceException;
import com.flightontime.entity.PrediccionEntity;
import com.flightontime.repository.PrediccionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;

@Service
public class FlightDelayService {

    //private static final String PYTHON_API_URL = "http://localhost:5000/predict";
    private static final String PYTHON_API_URL = "http://127.0.0.1:5000/predict";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PrediccionRepository prediccionRepository;

    @Transactional // Recomendado para asegurar que el guardado en DB sea consistente
    public FlightDelayResponseDto predictDelay(FlightDelayRequestDto requestDto) {

        try {
            // 1️⃣ Calcular día de la semana y si es fin de semana
            DayOfWeek dayOfWeek = requestDto.getFecha_salida().getDayOfWeek();
            int diaSemana = dayOfWeek.getValue(); // 1=Lunes ... 7=Domingo
            int esFinde = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) ? 1 : 0;

            // 2️⃣ Armar el DTO que espera el modelo de Python
            FlightDelayModelRequestDto modelDto = new FlightDelayModelRequestDto();
            modelDto.setAerolinea(requestDto.getAerolinea());
            modelDto.setHora_salida(requestDto.getHora_salida());
            modelDto.setDia_semana(diaSemana);
            modelDto.setEs_finde(esFinde);
            modelDto.setDistancia_km(requestDto.getDistancia_km());
            modelDto.setTaxi_out(requestDto.getTaxi_out());

            // Configurar cabeceras
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<FlightDelayModelRequestDto> request = new HttpEntity<>(modelDto, headers);

            // 3️⃣ Llamada al microservicio de Python (FastAPI/Flask)
            ResponseEntity<FlightDelayResponseDto> response = restTemplate.postForEntity(
                    PYTHON_API_URL,
                    request,
                    FlightDelayResponseDto.class
            );

            FlightDelayResponseDto responseBody = response.getBody();

            // 4️⃣ PERSISTENCIA: Guardar la predicción en MySQL
            if (responseBody != null) {
                PrediccionEntity entity = new PrediccionEntity();

                // Mapeo desde el Request original
                entity.setAirlineCode(requestDto.getAerolinea());
                // Nota: Si no tienes origen/destino en el DTO, puedes dejarlos nulos o asignar valores por defecto
                entity.setOrigin("N/A");
                entity.setDest("N/A");
                entity.setFlightDate(requestDto.getFecha_salida());

                // Mapeo desde la respuesta de Python
                entity.setProbabilidadRetraso(responseBody.getProbabilidad_retraso());
                entity.setRetrasado(responseBody.getRetrasado());
                ///entity.setRetrasado(responseBody.getPrediccion()); // "SÍ" o "NO"

                // Guardar físicamente en la tabla 'predicciones_vuelos'
                prediccionRepository.save(entity);
            }

            return responseBody;

        } catch (HttpClientErrorException.BadRequest e) {
            throw new AerolineaNoEncontradaException(extraerMensaje(e));
        } catch (HttpServerErrorException e) {
            throw new ExternalServiceException("Error al comunicarse con el microservicio de predicción");
        } catch (Exception e) {
            throw new ExternalServiceException("Error interno al procesar la predicción: " + e.getMessage());
        }
    }

    private String extraerMensaje(HttpClientErrorException.BadRequest e) {
        try {
            String body = e.getResponseBodyAsString();
            if (body == null || body.isBlank()) return "Aerolínea no soportada";

            int start = body.indexOf("\"detail\":\"");
            if (start != -1) {
                start += 10;
                int end = body.indexOf("\"", start);
                if (end != -1) return body.substring(start, end);
            }
            return body;
        } catch (Exception ex) {
            return "Aerolínea no soportada";
        }
    }
}