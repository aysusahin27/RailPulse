package com.basespringboot.service;

import com.basespringboot.dto.StationRequest;
import com.basespringboot.dto.StationResponse;
import com.basespringboot.dto.TripResponse;
import com.basespringboot.entity.StationEntity;
import com.basespringboot.entity.TripEntity;
import com.basespringboot.exception.DuplicateResourceException;
import com.basespringboot.exception.ResourceNotFoundException;
import com.basespringboot.repository.StationRepository;
import com.basespringboot.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;
    private final TripRepository tripRepository;

    @Override
    @Transactional
    public StationResponse save(StationRequest request) {
        String uicCode = request.getUicCode().trim();
        if (stationRepository.existsByUicCode(uicCode)) {
            throw new DuplicateResourceException("Station with UIC code '" + uicCode + "' already exists");
        }

        StationEntity station = new StationEntity();
        station.setName(request.getName().trim());
        station.setCity(request.getCity() != null && !request.getCity().isBlank() ? request.getCity().trim() : request.getName().trim());
        station.setUicCode(uicCode);
        return mapToResponse(stationRepository.save(station));
    }

    @Override
    public List<StationResponse> findAll() {
        return stationRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<StationResponse> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        return stationRepository.search(query.trim()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public StationResponse findById(Long id) {
        StationEntity station = stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Station with id " + id + " not found"));
        return mapToResponse(station);
    }

    @Override
    public List<TripResponse> getTripsByStation(Long id) {
        if (!stationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Station with id " + id + " not found");
        }
        return tripRepository.findByDepartureStationIdOrderByDepartureTimeAsc(id).stream()
                .map(this::mapTripToResponse)
                .toList();
    }

    @Override
    @Transactional
    public StationResponse update(Long id, StationRequest request) {
        StationEntity station = stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Station with id " + id + " not found"));

        String uicCode = request.getUicCode().trim();
        if (stationRepository.existsByUicCodeAndIdNot(uicCode, id)) {
            throw new DuplicateResourceException("Station with UIC code '" + uicCode + "' already exists");
        }

        station.setName(request.getName().trim());
        if (request.getCity() != null && !request.getCity().isBlank()) {
            station.setCity(request.getCity().trim());
        }
        station.setUicCode(uicCode);
        return mapToResponse(stationRepository.save(station));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!stationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Station with id " + id + " not found");
        }
        stationRepository.deleteById(id);
    }

    private StationResponse mapToResponse(StationEntity entity) {
        int tripsCount = entity.getTrips() != null ? entity.getTrips().size() : 0;
        return new StationResponse(entity.getId(), entity.getName(), entity.getCity(), entity.getUicCode(), tripsCount);
    }

    private TripResponse mapTripToResponse(TripEntity entity) {
        StationEntity st = entity.getDepartureStation();
        StationResponse stationResponse = null;
        if (st != null) {
            stationResponse = new StationResponse(st.getId(), st.getName(), st.getCity(), st.getUicCode());
        }
        return new TripResponse(
                entity.getId(),
                entity.getLabel(),
                entity.getTrainType(),
                entity.getDestinationName(),
                entity.getDepartureTime(),
                entity.getArrivalTime(),
                entity.getTrack(),
                entity.getStatus(),
                entity.getStatusMessage(),
                stationResponse
        );
    }
}
