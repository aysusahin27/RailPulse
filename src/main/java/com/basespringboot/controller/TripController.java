package com.basespringboot.controller;

import com.basespringboot.dto.TripRequest;
import com.basespringboot.dto.TripResponse;
import com.basespringboot.security.RequireAuth;
import com.basespringboot.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequireAuth
    public TripResponse create(@Valid @RequestBody TripRequest request) {
        return tripService.save(request);
    }

    @GetMapping
    public List<TripResponse> getAll(@RequestParam(value = "stationId", required = false) Long stationId) {
        if (stationId != null) {
            return tripService.findByStation(stationId);
        }
        return tripService.findAll();
    }

    @GetMapping("/search")
    public List<TripResponse> search(
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "q", required = false) String query
    ) {
        String searchTerm = (city != null && !city.isBlank()) ? city : query;
        return tripService.search(searchTerm);
    }

    @GetMapping("/{id}")
    public TripResponse getById(@PathVariable Long id) {
        return tripService.findById(id);
    }

    @PutMapping("/{id}")
    @RequireAuth
    public TripResponse update(@PathVariable Long id, @Valid @RequestBody TripRequest request) {
        return tripService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequireAuth
    public void delete(@PathVariable Long id) {
        tripService.delete(id);
    }
}
