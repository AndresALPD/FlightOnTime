package com.flightontime.service;

import com.flightontime.dto.AirlineDto;
import com.flightontime.dto.DestinationDto;
import com.flightontime.dto.DistanceDto;
import com.flightontime.dto.OriginDto;
import com.flightontime.repository.DestinoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final DestinoRepository destinoRepository;

    public List<AirlineDto> getAirlines() {
        return destinoRepository.findDistinctAirlines()
                .stream()
                .map(row -> new AirlineDto(
                        (String) row[0],
                        (String) row[1]
                ))
                .toList();
    }

    public List<OriginDto> getOriginsByAirline(String airline) {
        return destinoRepository.findDistinctOriginsByAirline(airline)
                .stream()
                .map(row -> new OriginDto(
                        (String) row[0],
                        (String) row[1]
                ))
                .toList();
    }

    public List<DestinationDto> getDestinations(String airline, String origin) {
        return destinoRepository.findDistinctDestinations(airline, origin)
                .stream()
                .map(row -> new DestinationDto(
                        (String) row[0],
                        (String) row[1]
                ))
                .toList();
    }

    public DistanceDto getDistance(
            String airline,
            String origin,
            String dest
    ) {
        List<Object[]> result =
                destinoRepository.findDistanceAndTaxiOut(
                        airline, origin, dest
                );

        if (result.isEmpty()) {
            throw new RuntimeException("No se encontró distancia para la ruta seleccionada");
        }

        Object[] row = result.get(0);

        Number distance = (Number) row[0];
        Number taxiOut = (Number) row[1];

        return new DistanceDto(
                distance.intValue(),
                taxiOut != null ? taxiOut.intValue() : 0
        );
    }

}
