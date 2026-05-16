package com.clone.BookMyShow.service.impl;

import com.clone.BookMyShow.dto.TheaterRequest;
import com.clone.BookMyShow.dto.TheaterResponse;
import com.clone.BookMyShow.entity.City;
import com.clone.BookMyShow.entity.Role;
import com.clone.BookMyShow.entity.Theater;
import com.clone.BookMyShow.entity.User;
import com.clone.BookMyShow.exception.ResourceAlreadyExistsException;
import com.clone.BookMyShow.exception.ResourceNotFoundException;
import com.clone.BookMyShow.repository.CityRepository;
import com.clone.BookMyShow.repository.TheaterRepository;
import com.clone.BookMyShow.repository.UserRepository;
import com.clone.BookMyShow.security.CustomUserDetails;
import com.clone.BookMyShow.service.TheaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TheaterServiceImpl implements TheaterService {

    private final TheaterRepository theaterRepository;
    private final CityRepository cityRepository;
    private final UserRepository userRepository;

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

        User owner = userRepository.findById(theaterRequest.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner user not found with id: " + theaterRequest.getOwnerId()));

        if (owner.getRole() != Role.ADMIN && owner.getRole() != Role.THEATER_OWNER) {
            owner.setRole(Role.THEATER_OWNER);
            userRepository.save(owner);
        }

        Theater theater = new Theater();
        theater.setName(theaterRequest.getName());
        theater.setAddress(theaterRequest.getAddress());
        theater.setCity(city);
        theater.setOwner(owner);
        theater.setActive(theaterRequest.isActive());

        Theater savedTheater = theaterRepository.save(theater);
        return mapToResponse(savedTheater);
    }

    @Override
    public TheaterResponse updateTheater(Long id, TheaterRequest theaterRequest) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));

        validateOwnership(theater);

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

        User owner=userRepository.findById(theaterRequest.getOwnerId()).
                orElseThrow(() -> new ResourceNotFoundException("user not found with id "+theaterRequest.getOwnerId()));

        if (owner.getRole() != Role.ADMIN && owner.getRole() != Role.THEATER_OWNER) {
            owner.setRole(Role.THEATER_OWNER);
            userRepository.save(owner);
        }


        theater.setName(theaterRequest.getName());
        theater.setAddress(theaterRequest.getAddress());
        theater.setCity(city);
        theater.setOwner(owner);
        if (theaterRequest.getIsActive() != null) {
            theater.setActive(theaterRequest.getIsActive());
        }


        Theater updatedTheater = theaterRepository.save(theater);
        return mapToResponse(updatedTheater);
    }

    @Override
    public void deleteTheater(Long id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));
        
        validateOwnership(theater);
        
        theater.setActive(false);
        theaterRepository.save(theater);
    }

    private void validateOwnership(Theater theater) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTheaterOwner = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_THEATER_OWNER"));
        
        if (isAdmin) return;
        
        if (isTheaterOwner && theater.getOwner().getId().equals(currentUser.getId())) {
            return;
        }
        
        throw new AccessDeniedException("You do not have permission to modify this theater.");
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
                .ownerId(theater.getOwner().getId())
                .ownerName(theater.getOwner().getName())
                .isActive(theater.isActive())
                .createdAt(theater.getCreatedAt())
                .updatedAt(theater.getUpdatedAt())
                .build();
    }
}
