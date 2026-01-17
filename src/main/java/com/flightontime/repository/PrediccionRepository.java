package com.flightontime.repository;

import com.flightontime.entity.PrediccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PrediccionRepository
        extends JpaRepository<PrediccionEntity, Long> {

        // Cuenta el total de registros para un día específico
        long countByFlightDate(LocalDate flightDate);

        // Cuenta cuántos vuelos fueron marcados como retrasados en ese día
        // Nota: Si en tu base de datos el campo 'retrasado' es String usa String, si es Boolean usa boolean.
        long countByFlightDateAndRetrasado(LocalDate flightDate, String retrasado);
}