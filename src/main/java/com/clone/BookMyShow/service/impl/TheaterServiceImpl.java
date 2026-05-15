package com.clone.BookMyShow.service.impl;

import com.clone.BookMyShow.dto.TheaterRequest;
import com.clone.BookMyShow.dto.TheaterResponse;
import com.clone.BookMyShow.entity.City;
import com.clone.BookMyShow.entity.Theater;
import com.clone.BookMyShow.exception.ResourceAlreadyExistsException;
import com.clone.BookMyShow.exception.ResourceNotFoundException;
import com.clone.BookMyShow.repository.CityRepository;
import com.clone.BookMyShow.repository.TheaterRepository;
import com.clone.BookMyShow.service.TheaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TheaterServiceImpl implements TheaterService {

    private final TheaterRepository theaterRepository;
    private final CityRepository cityRepository;

    @Override
    public TheaterResponse addTheater(TheaterRequest theaterRequest) {
        City city = cityRepository.findById(theaterRequest.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + theaterRequest.getCityId()));

        if (!city.isActive()) {
            throw new ResourceNotFoundException("Cannot add theater to city '" + city.getName() + "' because the city is currently inactive.");
        }

        if (theaterRepository.existsByNameIgnoreCaseAndCityId(theaterRequest.getName(), theaterRequest.getCityId())) {
            throw new ResourceAlreadyExistsException("Theater with name '" + theaterRequest.getName() + 
                    "' already exists in city " + city.getName());
        }

        Theater theater = new Theater();
        theater.setName(theaterRequest.getName());
        theater.setAddress(theaterRequest.getAddress());
        theater.setCity(city);
        theater.setActive(theaterRequest.isActive());

        Theater savedTheater = theaterRepository.save(theater);
        return mapToResponse(savedTheater);
    }

    @Override
    public TheaterResponse updateTheater(Long id, TheaterRequest theaterRequest) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));

        City city = cityRepository.findById(theaterRequest.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + theaterRequest.getCityId()));

        if (!city.isActive()) {
            throw new ResourceNotFoundException("Cannot update theater in city '" + city.getName() + "' because the city is currently inactive.");
        }

        if (!theater.getName().equalsIgnoreCase(theaterRequest.getName()) && 
                theaterRepository.existsByNameIgnoreCaseAndCityId(theaterRequest.getName(), theaterRequest.getCityId())) {
            throw new ResourceAlreadyExistsException("Theater with name '" + theaterRequest.getName() + 
                    "' already exists in city " + city.getName());
        }

        theater.setName(theaterRequest.getName());
        theater.setAddress(theaterRequest.getAddress());
        theater.setCity(city);
        theater.setActive(theaterRequest.isActive());

        Theater updatedTheater = theaterRepository.save(theater);
        return mapToResponse(updatedTheater);
    }

    @Override
    public void deleteTheater(Long id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));
        theater.setActive(false);
        theaterRepository.save(theater);
    }

    @Override
    public TheaterResponse getTheaterById(Long id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));
        return mapToResponse(theater);
    }

    @Override
    public List<TheaterResponse> getAllTheaters() {
        return theaterRepository.findAllActiveTheaters().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TheaterResponse> getTheatersByCity(Long cityId) {
        return theaterRepository.findActiveTheatersByCityId(cityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TheaterResponse mapToResponse(Theater theater) {
        return TheaterResponse.builder()
                .id(theater.getId())
                .name(theater.getName())
                .address(theater.getAddress())
                .cityId(theater.getCity().getId())
                .cityName(theater.getCity().getName())
                .isActive(theater.isActive())
                .createdAt(theater.getCreatedAt())
                .updatedAt(theater.getUpdatedAt())
                .build();
    }
}
