package com.basespringboot.dto;

public record TripResponse(
        Long id,
        String label,
        String trainType,
        String destinationName,
        String departureTime,
        String arrivalTime,
        String track,
        String status,
        String statusMessage,
        StationResponse departureStation
) {
    public TripResponse(Long id, String label, String destinationName, StationResponse departureStation) {
        this(id, label, "TGV INOUI", destinationName, "12:00", null, "Voie 1", "ON_TIME", null, departureStation);
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getTrainType() {
        return trainType;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getTrack() {
        return track;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public StationResponse getDepartureStation() {
        return departureStation;
    }
}
