package com.flightontime.repository;

import com.flightontime.entity.Destino;
import com.flightontime.entity.DestinoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DestinoRepository extends JpaRepository<Destino, DestinoId> {

    @Query("""
        SELECT DISTINCT d.airlineCode, d.airline
        FROM Destino d
        ORDER BY d.airline
    """)
    List<Object[]> findDistinctAirlines();

    @Query("""
    SELECT DISTINCT d.origin, d.originCity
    FROM Destino d
    WHERE d.airlineCode = :airline
    ORDER BY d.origin
""")
    List<Object[]> findDistinctOriginsByAirline(String airline);

    @Query("""
    SELECT DISTINCT d.dest, d.destCity
    FROM Destino d
    WHERE d.airlineCode = :airline
      AND d.origin = :origin
    ORDER BY d.dest
""")
    List<Object[]> findDistinctDestinations(String airline, String origin);

    @Query("""
    SELECT d.distance, d.taxiOut
    FROM Destino d
    WHERE d.airlineCode = :airline
      AND d.origin = :origin
      AND d.dest = :dest
""")
    List<Object[]> findDistanceAndTaxiOut(
            @Param("airline") String airline,
            @Param("origin") String origin,
            @Param("dest") String dest
    );

}

