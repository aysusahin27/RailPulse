package com.basespringboot.dto;

public record StationResponse(
        Long id,
        String name,
        String city,
        String uicCode,
        int tripsCount
) {
    public StationResponse(Long id, String name, String city, String uicCode) {
        this(id, name, city, uicCode, 0);
    }

    public StationResponse(Long id, String name, String uicCode) {
        this(id, name, name, uicCode, 0);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getUicCode() {
        return uicCode;
    }

    public int getTripsCount() {
        return tripsCount;
    }
}
