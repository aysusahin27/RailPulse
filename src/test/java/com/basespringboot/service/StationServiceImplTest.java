package com.basespringboot.service;

import com.basespringboot.dto.StationRequest;
import com.basespringboot.dto.StationResponse;
import com.basespringboot.dto.TripResponse;
import com.basespringboot.entity.StationEntity;
import com.basespringboot.entity.TripEntity;
import com.basespringboot.exception.DuplicateResourceException;
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
class StationServiceImplTest {

    @Mock
    private StationRepository stationRepository;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private StationServiceImpl stationService;

    private StationEntity stationEntity;
    private StationRequest stationRequest;

    @BeforeEach
    void setUp() {
        stationEntity = new StationEntity("Gare de Lyon", "Paris", "87271007");
        stationEntity.setId(1L);

        stationRequest = StationRequest.builder()
                .name("Gare de Lyon")
                .city("Paris")
                .uicCode("87271007")
                .build();
    }

    @Test
    void save_Success() {
        when(stationRepository.existsByUicCode("87271007")).thenReturn(false);
        when(stationRepository.save(any(StationEntity.class))).thenReturn(stationEntity);

        StationResponse response = stationService.save(stationRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Gare de Lyon");
        assertThat(response.getCity()).isEqualTo("Paris");
        assertThat(response.getUicCode()).isEqualTo("87271007");

        verify(stationRepository).existsByUicCode("87271007");
        verify(stationRepository).save(any(StationEntity.class));
    }

    @Test
    void save_DuplicateUicCode_ThrowsDuplicateResourceException() {
        when(stationRepository.existsByUicCode("87271007")).thenReturn(true);

        assertThatThrownBy(() -> stationService.save(stationRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("87271007");

        verify(stationRepository, never()).save(any());
    }

    @Test
    void findAll_ReturnsList() {
        StationEntity station2 = new StationEntity("Gare Montparnasse", "Paris", "87391003");
        station2.setId(2L);
        when(stationRepository.findAll()).thenReturn(List.of(stationEntity, station2));

        List<StationResponse> list = stationService.findAll();

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getName()).isEqualTo("Gare de Lyon");
        assertThat(list.get(1).getName()).isEqualTo("Gare Montparnasse");
    }

    @Test
    void search_ReturnsFilteredStations() {
        when(stationRepository.search("Paris")).thenReturn(List.of(stationEntity));

        List<StationResponse> list = stationService.search("Paris");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getCity()).isEqualTo("Paris");
    }

    @Test
    void findById_Success() {
        when(stationRepository.findById(1L)).thenReturn(Optional.of(stationEntity));

        StationResponse response = stationService.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Gare de Lyon");
    }

    @Test
    void findById_NotFound_ThrowsResourceNotFoundException() {
        when(stationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stationService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getTripsByStation_Success() {
        TripEntity trip = new TripEntity("TGV 6601", "TGV INOUI", "Marseille", "08:00", "11:00", "Voie 1", "ON_TIME", null, stationEntity);
        trip.setId(10L);

        when(stationRepository.existsById(1L)).thenReturn(true);
        when(tripRepository.findByDepartureStationIdOrderByDepartureTimeAsc(1L)).thenReturn(List.of(trip));

        List<TripResponse> trips = stationService.getTripsByStation(1L);

        assertThat(trips).hasSize(1);
        assertThat(trips.get(0).getLabel()).isEqualTo("TGV 6601");
        assertThat(trips.get(0).getDepartureStation().getName()).isEqualTo("Gare de Lyon");
    }

    @Test
    void update_Success() {
        when(stationRepository.findById(1L)).thenReturn(Optional.of(stationEntity));
        when(stationRepository.existsByUicCodeAndIdNot("87271007", 1L)).thenReturn(false);
        when(stationRepository.save(any(StationEntity.class))).thenReturn(stationEntity);

        StationResponse response = stationService.update(1L, stationRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Gare de Lyon");
        verify(stationRepository).save(stationEntity);
    }

    @Test
    void delete_Success() {
        when(stationRepository.existsById(1L)).thenReturn(true);

        stationService.delete(1L);

        verify(stationRepository).deleteById(1L);
    }

    @Test
    void delete_NotFound_ThrowsResourceNotFoundException() {
        when(stationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> stationService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(stationRepository, never()).deleteById(any());
    }
}
