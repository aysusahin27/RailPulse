package com.basespringboot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "stations")
@Getter
@Setter
@NoArgsConstructor
public class StationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "uic_code", nullable = false, unique = true)
    private String uicCode;

    @OneToMany(mappedBy = "departureStation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripEntity> trips = new ArrayList<>();

    public StationEntity(String name, String city, String uicCode) {
        this.name = name;
        this.city = city;
        this.uicCode = uicCode;
    }

    public void addTrip(TripEntity trip) {
        trips.add(trip);
        trip.setDepartureStation(this);
    }

    public void removeTrip(TripEntity trip) {
        trips.remove(trip);
        trip.setDepartureStation(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StationEntity that = (StationEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
