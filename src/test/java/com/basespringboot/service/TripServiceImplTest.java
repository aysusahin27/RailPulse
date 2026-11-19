package com.basespringboot.service;

import com.basespringboot.dto.TripRequest;
import com.basespringboot.dto.TripResponse;
import com.basespringboot.entity.StationEntity;
import com.basespringboot.entity.TripEntity;
import com.basespringboot.exception.ResourceNotFoundException;
import com.basespringboot.repository.StationRepository;
import com.basespringboot.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceImplTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private TripServiceImpl tripService;

    private StationEntity stationEntity;
    private TripEntity tripEntity;
    private TripRequest tripRequest;

    @BeforeEach
    void setUp() {
        stationEntity = new StationEntity("Gare de Lyon", "Paris", "87271007");
        stationEntity.setId(1L);

        tripEntity = new TripEntity("TGV 6601", "TGV INOUI", "Marseille Saint-Charles", "08:00", "11:15", "Voie 1", "ON_TIME", "À l'heure", stationEntity);
        tripEntity.setId(10L);

        tripRequest = TripRequest.builder()
                .label("TGV 6601")
                .trainType("TGV INOUI")
                .destinationName("Marseille Saint-Charles")
                .departureTime("08:00")
                .arrivalTime("11:15")
                .track("Voie 1")
                .status("ON_TIME")
                .departureStationId(1L)
                .build();
    }

    @Test
    void save_Success() {
        when(stationRepository.findById(1L)).thenReturn(Optional.of(stationEntity));
        when(tripRepository.save(any(TripEntity.class))).thenReturn(tripEntity);

        TripResponse response = tripService.save(tripRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getLabel()).isEqualTo("TGV 6601");
        assertThat(response.getTrainType()).isEqualTo("TGV INOUI");
        assertThat(response.getDestinationName()).isEqualTo("Marseille Saint-Charles");
        assertThat(response.getDepartureTime()).isEqualTo("08:00");
        assertThat(response.getDepartureStation()).isNotNull();
        assertThat(response.getDepartureStation().getName()).isEqualTo("Gare de Lyon");

        verify(stationRepository).findById(1L);
        verify(tripRepository).save(any(TripEntity.class));
    }

    @Test
    void save_StationNotFound_ThrowsResourceNotFoundException() {
        when(stationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.save(tripRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Station with id 1 not found");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void findAll_ReturnsList() {
        TripEntity trip2 = new TripEntity("TER 8802", "TER", "Dijon", "09:00", "11:30", "Voie 2", "ON_TIME", null, stationEntity);
        trip2.setId(20L);
        when(tripRepository.findAll()).thenReturn(List.of(tripEntity, trip2));

        List<TripResponse> list = tripService.findAll();

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getLabel()).isEqualTo("TGV 6601");
        assertThat(list.get(1).getLabel()).isEqualTo("TER 8802");
    }

    @Test
    void search_ReturnsMatchingTrips() {
        when(tripRepository.search("Paris")).thenReturn(List.of(tripEntity));

        List<TripResponse> list = tripService.search("Paris");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getLabel()).isEqualTo("TGV 6601");
        verify(tripRepository).search("Paris");
    }

    @Test
    void findById_Success() {
        when(tripRepository.findById(10L)).thenReturn(Optional.of(tripEntity));

        TripResponse response = tripService.findById(10L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getLabel()).isEqualTo("TGV 6601");
    }

    @Test
    void findById_NotFound_ThrowsResourceNotFoundException() {
        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trip with id 999 not found");
    }

    @Test
    void update_Success() {
        when(tripRepository.findById(10L)).thenReturn(Optional.of(tripEntity));
        when(stationRepository.findById(1L)).thenReturn(Optional.of(stationEntity));
        when(tripRepository.save(any(TripEntity.class))).thenReturn(tripEntity);

        TripResponse response = tripService.update(10L, tripRequest);

        assertThat(response).isNotNull();
        verify(tripRepository).save(tripEntity);
    }

    @Test
    void delete_Success() {
        when(tripRepository.existsById(10L)).thenReturn(true);

        tripService.delete(10L);

        verify(tripRepository).deleteById(10L);
    }

    @Test
    void delete_NotFound_ThrowsResourceNotFoundException() {
        when(tripRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> tripService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(tripRepository, never()).deleteById(any());
    }
}
