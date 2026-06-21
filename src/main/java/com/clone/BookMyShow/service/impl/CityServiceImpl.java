package com.clone.BookMyShow.service.impl;

import com.clone.BookMyShow.dto.CityRequest;
import com.clone.BookMyShow.dto.CityResponse;
import com.clone.BookMyShow.entity.City;
import com.clone.BookMyShow.exception.ResourceAlreadyExistsException;
import com.clone.BookMyShow.exception.ResourceNotFoundException;
import com.clone.BookMyShow.repository.CityRepository;
import com.clone.BookMyShow.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    @Override
    @CacheEvict(value = "cities", allEntries = true)
    public CityResponse addCity(CityRequest cityRequest) {
        if (cityRepository.existsByNameIgnoreCase(cityRequest.getName())) {
            throw new ResourceAlreadyExistsException("City with name '" + cityRequest.getName() + "' already exists.");
        }
        City city = new City();
        city.setName(cityRequest.getName());
        city.setIsActive(cityRequest.getIsActive() != null ? cityRequest.getIsActive() : true);
        City savedCity = cityRepository.save(city);
        return mapToResponse(savedCity);
    }

    @Override
    @CacheEvict(value = "cities", allEntries = true)
    public CityResponse updateCity(Long id, CityRequest cityRequest) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));
        
        if (!city.getName().equalsIgnoreCase(cityRequest.getName()) && cityRepository.existsByNameIgnoreCase(cityRequest.getName())) {
            throw new ResourceAlreadyExistsException("City with name '" + cityRequest.getName() + "' already exists.");
        }
        
        city.setName(cityRequest.getName());
        if (cityRequest.getIsActive() != null) {
            city.setIsActive(cityRequest.getIsActive());
        }
        City updatedCity = cityRepository.save(city);
        return mapToResponse(updatedCity);
    }

    @Override
    @CacheEvict(value = "cities", allEntries = true)
    public void deleteCity(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));
        city.setIsActive(false);
        cityRepository.save(city);
    }

    @Override
    @Cacheable(value = "cities", key = "#id")
    public CityResponse getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));
        return mapToResponse(city);
    }

    @Override
    @Cacheable(value = "cities", key = "'all'")
    public List<CityResponse> getAllCities() {
        return cityRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "cities", key = "'active'")
    public List<CityResponse> getActiveCities() {
        return cityRepository.findAll().stream()
                .filter(City::getIsActive)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CityResponse mapToResponse(City city) {
        return CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .isActive(city.getIsActive())
                .createdAt(city.getCreatedAt())
                .updatedAt(city.getUpdatedAt())
                .build();
    }
}
