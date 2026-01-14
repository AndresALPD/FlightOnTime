package com.flightontime.repository;

import com.flightontime.entity.PrediccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrediccionRepository
        extends JpaRepository<PrediccionEntity, Long> {
}