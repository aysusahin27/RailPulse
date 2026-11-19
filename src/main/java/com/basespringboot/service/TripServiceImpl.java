package com.basespringboot.service;

import com.basespringboot.dto.StationResponse;
import com.basespringboot.dto.TripRequest;
import com.basespringboot.dto.TripResponse;
import com.basespringboot.entity.StationEntity;
import com.basespringboot.entity.TripEntity;
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
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final StationRepository stationRepository;

    @Override
    @Transactional
    public TripResponse save(TripRequest request) {
        StationEntity station = stationRepository.findById(request.getDepartureStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Station with id " + request.getDepartureStationId() + " not found"));

        TripEntity trip = new TripEntity();
        trip.setLabel(request.getLabel().trim());
        trip.setTrainType(request.getTrainType() != null && !request.getTrainType().isBlank() ? request.getTrainType().trim() : "TGV INOUI");
        trip.setDestinationName(request.getDestinationName().trim());
        trip.setDepartureTime(request.getDepartureTime() != null && !request.getDepartureTime().isBlank() ? request.getDepartureTime().trim() : "12:00");
        trip.setArrivalTime(request.getArrivalTime() != null ? request.getArrivalTime().trim() : null);
        trip.setTrack(request.getTrack() != null && !request.getTrack().isBlank() ? request.getTrack().trim() : "Voie 1");
        trip.setStatus(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus().trim() : "ON_TIME");
        trip.setStatusMessage(request.getStatusMessage() != null ? request.getStatusMessage().trim() : null);
        trip.setDepartureStation(station);

        return mapToResponse(tripRepository.save(trip));
    }

    @Override
    public List<TripResponse> findAll() {
        return tripRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<TripResponse> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        return tripRepository.search(query.trim()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<TripResponse> findByStation(Long stationId) {
        return tripRepository.findByDepartureStationIdOrderByDepartureTimeAsc(stationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public TripResponse findById(Long id) {
        TripEntity trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip with id " + id + " not found"));
        return mapToResponse(trip);
    }

    @Override
    @Transactional
    public TripResponse update(Long id, TripRequest request) {
        TripEntity trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip with id " + id + " not found"));

        StationEntity station = stationRepository.findById(request.getDepartureStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Station with id " + request.getDepartureStationId() + " not found"));

        trip.setLabel(request.getLabel().trim());
        if (request.getTrainType() != null && !request.getTrainType().isBlank()) {
            trip.setTrainType(request.getTrainType().trim());
        }
        trip.setDestinationName(request.getDestinationName().trim());
        if (request.getDepartureTime() != null && !request.getDepartureTime().isBlank()) {
            trip.setDepartureTime(request.getDepartureTime().trim());
        }
        trip.setArrivalTime(request.getArrivalTime() != null ? request.getArrivalTime().trim() : null);
        if (request.getTrack() != null && !request.getTrack().isBlank()) {
            trip.setTrack(request.getTrack().trim());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            trip.setStatus(request.getStatus().trim());
        }
        trip.setStatusMessage(request.getStatusMessage() != null ? request.getStatusMessage().trim() : null);
        trip.setDepartureStation(station);

        return mapToResponse(tripRepository.save(trip));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!tripRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trip with id " + id + " not found");
        }
        tripRepository.deleteById(id);
    }

    private TripResponse mapToResponse(TripEntity entity) {
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
