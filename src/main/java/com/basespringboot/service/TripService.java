package com.basespringboot.service;

import com.basespringboot.dto.TripRequest;
import com.basespringboot.dto.TripResponse;

import java.util.List;

public interface TripService {
    TripResponse save(TripRequest request);
    List<TripResponse> findAll();
    List<TripResponse> search(String query);
    List<TripResponse> findByStation(Long stationId);
    TripResponse findById(Long id);
    TripResponse update(Long id, TripRequest request);
    void delete(Long id);
}
