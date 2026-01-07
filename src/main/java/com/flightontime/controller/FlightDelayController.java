package com.flightontime.controller;

import com.flightontime.dto.FlightDelayRequestDto;
import com.flightontime.dto.FlightDelayResponseDto;
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
}
