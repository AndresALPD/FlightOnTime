package com.flightontimeAM.service;

import com.flightontimeAM.dto.FlightRequest;
import com.flightontimeAM.dto.PredictionResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class PredictionService {

    private final RestTemplate restTemplate;
    private static final String PYTHON_SERVICE_URL = "http://localhost:5000/predict";

    public PredictionService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Realiza la predicción llamando al microservicio Python
     */
    public PredictionResponse predict(FlightRequest request) {
        try {
            System.out.println("🔮 Llamando al servicio Python...");

            // Parsear la fecha para extraer hora y día de la semana
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime fechaPartida = LocalDateTime.parse(request.getFechaPartida(), formatter);

            int horaSalida = fechaPartida.getHour();
            int diaSemana = fechaPartida.getDayOfWeek().getValue(); // Lunes=1, Domingo=7

            // Preparar el payload para el servicio Python
            Map<String, Object> payload = new HashMap<>();
            payload.put("aerolinea", request.getAerolinea().toUpperCase());
            payload.put("origen", request.getOrigen().toUpperCase());
            payload.put("destino", request.getDestino().toUpperCase());
            payload.put("hora_salida", horaSalida);
            payload.put("dia_semana", diaSemana);
            payload.put("distancia_km", request.getDistanciaKm());

            System.out.println("📤 Enviando datos a Python: " + payload);

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            // Llamar al servicio Python
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    PYTHON_SERVICE_URL,
                    entity,
                    Map.class
            );

            Map<String, Object> body = response.getBody();

            if (body != null) {
                String prevision = (String) body.get("prevision");
                Double probabilidad = ((Number) body.get("probabilidad")).doubleValue();

                System.out.println("✅ Respuesta de Python: " + prevision + " (" + probabilidad + ")");

                return new PredictionResponse(prevision, probabilidad);
            }

            throw new RuntimeException("Respuesta vacía del servicio de predicción");

        } catch (HttpClientErrorException e) {
            // 🔴 Error 4xx desde Python (ej: aerolínea inválida)
            throw new RuntimeException(
                    "Error de validación desde Python: " + e.getResponseBodyAsString()
            );
        } catch (Exception e) {
            // 🟡 Error real del sistema → fallback permitido
            System.err.println("❌ Error al llamar a Python: " + e.getMessage());
            System.out.println("⚠️  Usando predicción simulada como fallback...");
            return predictSimulated(request);
        }
    }

    /**
     * Versión simulada como fallback
     */
    private PredictionResponse predictSimulated(FlightRequest request) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime fechaPartida = LocalDateTime.parse(request.getFechaPartida(), formatter);

            int horaSalida = fechaPartida.getHour();
            int diaSemana = fechaPartida.getDayOfWeek().getValue() - 1;

            double probabilidadRetraso = 0.2;

            if ((horaSalida >= 7 && horaSalida <= 9) || (horaSalida >= 18 && horaSalida <= 20)) {
                probabilidadRetraso += 0.25;
            }

            String origen = request.getOrigen().toUpperCase();
            if (origen.equals("GRU") || origen.equals("GIG") || origen.equals("MEX")) {
                probabilidadRetraso += 0.15;
            }

            if (request.getDistanciaKm() > 2000) {
                probabilidadRetraso += 0.15;
            }

            if (diaSemana >= 4) {
                probabilidadRetraso += 0.20;
            }

            probabilidadRetraso = Math.min(0.95, probabilidadRetraso);
            String prevision = probabilidadRetraso >= 0.5 ? "Retrasado" : "Puntual";

            return new PredictionResponse(prevision, probabilidadRetraso);

        } catch (Exception e) {
            throw new RuntimeException("Error en predicción simulada: " + e.getMessage(), e);
        }
    }
}