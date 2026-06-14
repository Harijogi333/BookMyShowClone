package com.clone.BookMyShow.service.impl;

import com.clone.BookMyShow.dto.CityRequest;
import com.clone.BookMyShow.dto.CityResponse;
import com.clone.BookMyShow.entity.City;
import com.clone.BookMyShow.exception.ResourceAlreadyExistsException;
import com.clone.BookMyShow.exception.ResourceNotFoundException;
import com.clone.BookMyShow.repository.CityRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CityServiceImplTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CityServiceImpl cityService;

    private City city;
    private CityRequest cityRequest;

    @BeforeEach
    void setUp() {
        city = new City();
        city.setId(1L);
        city.setName("Mumbai");
        city.setIsActive(true);
        city.setCreatedAt(LocalDateTime.now());
        city.setUpdatedAt(LocalDateTime.now());

        cityRequest = new CityRequest();
        cityRequest.setName("Mumbai");
        cityRequest.setIsActive(true);
    }

    @Test
    @DisplayName("Should successfully add a city")
    void addCity_Success() {
        log.info("Testing addCity success case");
        when(cityRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(cityRepository.save(any(City.class))).thenReturn(city);

        CityResponse response = cityService.addCity(cityRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(cityRequest.getName());
        
        ArgumentCaptor<City> cityCaptor = ArgumentCaptor.forClass(City.class);
        verify(cityRepository).save(cityCaptor.capture());
        assertThat(cityCaptor.getValue().getName()).isEqualTo(cityRequest.getName());
        verify(cityRepository, times(1)).existsByNameIgnoreCase(anyString());
    }

    @Test
    @DisplayName("Should throw ResourceAlreadyExistsException when adding a city that already exists")
    void addCity_Failure_AlreadyExists() {
        log.info("Testing addCity failure case: already exists");
        when(cityRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        assertThatThrownBy(() -> cityService.addCity(cityRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("City with name 'Mumbai' already exists.");
        
        verify(cityRepository, never()).save(any(City.class));
    }

    @Test
    @DisplayName("Should successfully update a city")
    void updateCity_Success() {
        log.info("Testing updateCity success case");
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(cityRepository.save(any(City.class))).thenReturn(city);

        CityResponse response = cityService.updateCity(1L, cityRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(cityRequest.getName());
        verify(cityRepository).save(any(City.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating a non-existent city")
    void updateCity_Failure_NotFound() {
        log.info("Testing updateCity failure case: not found");
        when(cityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cityService.updateCity(1L, cityRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("City not found with id: 1");
    }

    @Test
    @DisplayName("Should successfully delete (deactivate) a city")
    void deleteCity_Success() {
        log.info("Testing deleteCity success case");
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(cityRepository.save(any(City.class))).thenReturn(city);

        cityService.deleteCity(1L);

        ArgumentCaptor<City> cityCaptor = ArgumentCaptor.forClass(City.class);
        verify(cityRepository).save(cityCaptor.capture());
        assertThat(cityCaptor.getValue().getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should successfully get city by ID")
    void getCityById_Success() {
        log.info("Testing getCityById success case");
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));

        CityResponse response = cityService.getCityById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should successfully get all cities")
    void getAllCities_Success() {
        log.info("Testing getAllCities success case");
        when(cityRepository.findAll()).thenReturn(Collections.singletonList(city));

        List<CityResponse> responses = cityService.getAllCities();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Mumbai");
    }

    @Test
    @DisplayName("Should successfully get active cities")
    void getActiveCities_Success() {
        log.info("Testing getActiveCities success case");
        when(cityRepository.findAll()).thenReturn(Collections.singletonList(city));

        List<CityResponse> responses = cityService.getActiveCities();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).isActive()).isTrue();
    }
}
