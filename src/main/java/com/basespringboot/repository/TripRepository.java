package com.basespringboot.repository;

import com.basespringboot.entity.TripEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripRepository extends JpaRepository<TripEntity, Long> {

    @Override
    @EntityGraph(attributePaths = {"departureStation"})
    List<TripEntity> findAll();

    @EntityGraph(attributePaths = {"departureStation"})
    List<TripEntity> findByDepartureStationIdOrderByDepartureTimeAsc(Long departureStationId);

    List<TripEntity> findByDepartureStationId(Long departureStationId);

    boolean existsByDepartureStationId(Long departureStationId);

    @EntityGraph(attributePaths = {"departureStation"})
    @Query("SELECT t FROM TripEntity t WHERE " +
            "LOWER(t.departureStation.city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(t.departureStation.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(t.destinationName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(t.label) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "ORDER BY t.departureTime ASC")
    List<TripEntity> search(@Param("q") String q);
}
