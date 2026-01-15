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
    public ResponseEntity<FlightDelayStatsResponseDto> getStats(
            @RequestParam(required = false) String fecha
    ) {
        // El servicio se encargará de consultar el repositorio de MySQL
        FlightDelayStatsResponseDto stats = flightDelayService.getDailyStats(fecha);
        return ResponseEntity.ok(stats);
    }

}
