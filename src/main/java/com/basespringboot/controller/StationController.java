package com.basespringboot.controller;

import com.basespringboot.dto.StationRequest;
import com.basespringboot.dto.StationResponse;
import com.basespringboot.dto.TripResponse;
import com.basespringboot.security.RequireAuth;
import com.basespringboot.service.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequireAuth
    public StationResponse create(@Valid @RequestBody StationRequest request) {
        return stationService.save(request);
    }

    @GetMapping
    public List<StationResponse> getAll() {
        return stationService.findAll();
    }

    @GetMapping("/search")
    public List<StationResponse> search(@RequestParam(value = "q", required = false) String query) {
        return stationService.search(query);
    }

    @GetMapping("/{id}")
    public StationResponse getById(@PathVariable Long id) {
        return stationService.findById(id);
    }

    @GetMapping("/{id}/trips")
    public List<TripResponse> getTripsByStation(@PathVariable Long id) {
        return stationService.getTripsByStation(id);
    }

    @PutMapping("/{id}")
    @RequireAuth
    public StationResponse update(@PathVariable Long id, @Valid @RequestBody StationRequest request) {
        return stationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequireAuth
    public void delete(@PathVariable Long id) {
        stationService.delete(id);
    }
}
