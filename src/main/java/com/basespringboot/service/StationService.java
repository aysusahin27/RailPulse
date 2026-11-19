package com.basespringboot.service;

import com.basespringboot.dto.StationRequest;
import com.basespringboot.dto.StationResponse;
import com.basespringboot.dto.TripResponse;

import java.util.List;

public interface StationService {
    StationResponse save(StationRequest request);
    List<StationResponse> findAll();
    List<StationResponse> search(String query);
    StationResponse findById(Long id);
    List<TripResponse> getTripsByStation(Long id);
    StationResponse update(Long id, StationRequest request);
    void delete(Long id);
}
