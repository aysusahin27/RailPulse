package com.basespringboot.controller;

import com.basespringboot.dto.StationRequest;
import com.basespringboot.dto.StationResponse;
import com.basespringboot.dto.TripResponse;
import com.basespringboot.service.StationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationControllerTest {

    @Mock
    private StationService stationService;

    @InjectMocks
    private StationController stationController;

    private StationRequest request;
    private StationResponse response;

    @BeforeEach
    void setUp() {
        request = new StationRequest("Gare de Lyon", "Paris", "87271007");
        response = new StationResponse(1L, "Gare de Lyon", "Paris", "87271007");
    }

    @Test
    void create_ReturnsCreatedStation() {
        when(stationService.save(request)).thenReturn(response);

        StationResponse actual = stationController.create(request);

        assertThat(actual).isEqualTo(response);
        verify(stationService).save(request);
    }

    @Test
    void getAll_ReturnsAllStations() {
        List<StationResponse> list = List.of(response);
        when(stationService.findAll()).thenReturn(list);

        List<StationResponse> actual = stationController.getAll();

        assertThat(actual).isEqualTo(list);
        verify(stationService).findAll();
    }

    @Test
    void search_ReturnsMatchingStations() {
        when(stationService.search("Paris")).thenReturn(List.of(response));

        List<StationResponse> actual = stationController.search("Paris");

        assertThat(actual).hasSize(1);
        verify(stationService).search("Paris");
    }

    @Test
    void getById_ReturnsStation() {
        when(stationService.findById(1L)).thenReturn(response);

        StationResponse actual = stationController.getById(1L);

        assertThat(actual).isEqualTo(response);
        verify(stationService).findById(1L);
    }

    @Test
    void getTripsByStation_ReturnsTrips() {
        TripResponse trip = new TripResponse(10L, "TGV 6601", "Marseille", response);
        when(stationService.getTripsByStation(1L)).thenReturn(List.of(trip));

        List<TripResponse> actual = stationController.getTripsByStation(1L);

        assertThat(actual).hasSize(1);
        verify(stationService).getTripsByStation(1L);
    }

    @Test
    void update_ReturnsUpdatedStation() {
        when(stationService.update(1L, request)).thenReturn(response);

        StationResponse actual = stationController.update(1L, request);

        assertThat(actual).isEqualTo(response);
        verify(stationService).update(1L, request);
    }

    @Test
    void delete_DeletesStation() {
        stationController.delete(1L);
        verify(stationService).delete(1L);
    }
}
