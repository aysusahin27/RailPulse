package com.basespringboot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
public class TripEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "train_type", nullable = false)
    private String trainType = "TGV INOUI";

    @Column(name = "destination_name", nullable = false)
    private String destinationName;

    @Column(name = "departure_time", nullable = false)
    private String departureTime = "12:00";

    @Column(name = "arrival_time")
    private String arrivalTime;

    @Column(name = "track")
    private String track = "Voie 1";

    @Column(name = "status", nullable = false)
    private String status = "ON_TIME"; // ON_TIME, DELAYED, BOARDING, CANCELLED

    @Column(name = "status_message")
    private String statusMessage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private StationEntity departureStation;

    public TripEntity(String label, String trainType, String destinationName, String departureTime, String arrivalTime, String track, String status, String statusMessage, StationEntity departureStation) {
        this.label = label;
        this.trainType = trainType != null ? trainType : "TGV INOUI";
        this.destinationName = destinationName;
        this.departureTime = departureTime != null ? departureTime : "12:00";
        this.arrivalTime = arrivalTime;
        this.track = track;
        this.status = status != null ? status : "ON_TIME";
        this.statusMessage = statusMessage;
        this.departureStation = departureStation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripEntity that = (TripEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
