package com.basespringboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripRequest {

    @NotBlank(message = "Trip label must not be blank")
    @Size(min = 1, max = 150, message = "Trip label must be between 1 and 150 characters")
    private String label;

    private String trainType; // e.g. TGV INOUI, TER, OUIGO, INTERCITÉS, EUROSTAR

    @NotBlank(message = "Destination name must not be blank")
    @Size(min = 1, max = 150, message = "Destination name must be between 1 and 150 characters")
    private String destinationName;

    private String departureTime; // e.g. "08:45"

    private String arrivalTime; // e.g. "11:52"

    private String track; // e.g. "Voie 2"

    private String status; // ON_TIME, DELAYED, BOARDING, CANCELLED

    private String statusMessage; // e.g. "Retard 15 min"

    @NotNull(message = "Departure station ID is required")
    @Positive(message = "Departure station ID must be a positive number")
    private Long departureStationId;

    public TripRequest(String label, String destinationName, Long departureStationId) {
        this.label = label;
        this.destinationName = destinationName;
        this.departureStationId = departureStationId;
        this.trainType = "TGV INOUI";
        this.departureTime = "12:00";
        this.track = "Voie 1";
        this.status = "ON_TIME";
    }
}
