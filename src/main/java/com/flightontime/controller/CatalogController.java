package com.flightontime.controller;

import com.flightontime.dto.AirlineDto;
import com.flightontime.dto.DestinationDto;
import com.flightontime.dto.DistanceDto;
import com.flightontime.dto.OriginDto;
import com.flightontime.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogs")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/airlines")
    public List<AirlineDto> getAirlines() {
        return catalogService.getAirlines();
    }

    @GetMapping("/origins")
    public List<OriginDto> getOrigins(@RequestParam String airline) {
        return catalogService.getOriginsByAirline(airline);
    }

    @GetMapping("/destinations")
    public List<DestinationDto> getDestinations(
            @RequestParam String airline,
            @RequestParam String origin
    ) {
        return catalogService.getDestinations(airline, origin);
    }

    @GetMapping("/distance")
    public DistanceDto getDistance(
            @RequestParam String airline,
            @RequestParam String origin,
            @RequestParam String dest
    ) {
        return catalogService.getDistance(airline, origin, dest);
    }

}
