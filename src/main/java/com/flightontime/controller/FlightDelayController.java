package com.flightontime.controller;

import com.flightontime.dto.FlightDelayRequestDto;
import com.flightontime.dto.FlightDelayResponseDto;
import com.flightontime.dto.FlightDelayStatsResponseDto;
import com.flightontime.service.FlightDelayService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flight-delay")
public class FlightDelayController {

    @Autowired
    private FlightDelayService flightDelayService;

    @PostMapping("/predict")
    public ResponseEntity<FlightDelayResponseDto> predictDelay(
            @Valid @RequestBody FlightDelayRequestDto requestDto
    ) {
        FlightDelayResponseDto response =
                flightDelayService.predictDelay(requestDto);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para obtener estadísticas desde MySQL
     * Se puede llamar como: /api/flight-delay/stats?fecha=2026-01-15
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats( // Cambiado a <?>
                                       @RequestParam(required = false) String fecha
    ) {
        if (fecha != null && !fecha.isEmpty()) {
            try {
                java.time.LocalDate.parse(fecha); // Intenta parsear YYYY-MM-DD
            } catch (java.time.format.DateTimeParseException e) {
                // Ahora el compilador aceptará este String de error
                return ResponseEntity.badRequest().body("Error: Formato de fecha inválido. Use YYYY-MM-DD");
            }
        }

        FlightDelayStatsResponseDto stats = flightDelayService.getDailyStats(fecha);
        return ResponseEntity.ok(stats);
    }

}
