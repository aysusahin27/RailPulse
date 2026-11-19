package com.basespringboot.repository;

import com.basespringboot.entity.StationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<StationEntity, Long> {

    Optional<StationEntity> findByUicCode(String uicCode);

    boolean existsByUicCode(String uicCode);

    boolean existsByUicCodeAndIdNot(String uicCode, Long id);

    List<StationEntity> findByCityIgnoreCase(String city);

    @Query("SELECT s FROM StationEntity s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(s.city) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY s.name ASC")
    List<StationEntity> search(@Param("q") String q);
}
