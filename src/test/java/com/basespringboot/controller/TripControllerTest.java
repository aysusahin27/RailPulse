package com.basespringboot.controller;

import com.basespringboot.dto.StationResponse;
import com.basespringboot.dto.TripRequest;
import com.basespringboot.dto.TripResponse;
import com.basespringboot.service.TripService;
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
class TripControllerTest {

    @Mock
    private TripService tripService;

    @InjectMocks
    private TripController tripController;

    private TripRequest request;
    private TripResponse response;

    @BeforeEach
    void setUp() {
        request = new TripRequest("TGV 6601", "Marseille Saint-Charles", 1L);
        StationResponse stationResponse = new StationResponse(1L, "Gare de Lyon", "Paris", "87271007");
        response = new TripResponse(10L, "TGV 6601", "Marseille Saint-Charles", stationResponse);
    }

    @Test
    void create_ReturnsCreatedTrip() {
        when(tripService.save(request)).thenReturn(response);

        TripResponse actual = tripController.create(request);

        assertThat(actual).isEqualTo(response);
        verify(tripService).save(request);
    }

    @Test
    void getAll_ReturnsAllTrips() {
        List<TripResponse> list = List.of(response);
        when(tripService.findAll()).thenReturn(list);

        List<TripResponse> actual = tripController.getAll(null);

        assertThat(actual).isEqualTo(list);
        verify(tripService).findAll();
    }

    @Test
    void getAll_FilteredByStation_ReturnsFilteredTrips() {
        List<TripResponse> list = List.of(response);
        when(tripService.findByStation(1L)).thenReturn(list);

        List<TripResponse> actual = tripController.getAll(1L);

        assertThat(actual).isEqualTo(list);
        verify(tripService).findByStation(1L);
    }

    @Test
    void search_ReturnsTrips() {
        when(tripService.search("Paris")).thenReturn(List.of(response));

        List<TripResponse> actual = tripController.search("Paris", null);

        assertThat(actual).hasSize(1);
        verify(tripService).search("Paris");
    }

    @Test
    void getById_ReturnsTrip() {
        when(tripService.findById(10L)).thenReturn(response);

        TripResponse actual = tripController.getById(10L);

        assertThat(actual).isEqualTo(response);
        verify(tripService).findById(10L);
    }

    @Test
    void update_ReturnsUpdatedTrip() {
        when(tripService.update(10L, request)).thenReturn(response);

        TripResponse actual = tripController.update(10L, request);

        assertThat(actual).isEqualTo(response);
        verify(tripService).update(10L, request);
    }

    @Test
    void delete_DeletesTrip() {
        tripController.delete(10L);
        verify(tripService).delete(10L);
    }
}
