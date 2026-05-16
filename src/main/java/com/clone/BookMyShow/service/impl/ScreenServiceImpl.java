package com.clone.BookMyShow.service.impl;

import com.clone.BookMyShow.dto.ScreenRequest;
import com.clone.BookMyShow.dto.ScreenResponse;
import com.clone.BookMyShow.entity.Screen;
import com.clone.BookMyShow.entity.Theater;
import com.clone.BookMyShow.exception.ResourceAlreadyExistsException;
import com.clone.BookMyShow.exception.ResourceNotFoundException;
import com.clone.BookMyShow.repository.ScreenRepository;
import com.clone.BookMyShow.repository.TheaterRepository;
import com.clone.BookMyShow.security.CustomUserDetails;
import com.clone.BookMyShow.service.ScreenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScreenServiceImpl implements ScreenService {

    private final ScreenRepository screenRepository;
    private final TheaterRepository theaterRepository;

    @Override
    public ScreenResponse addScreen(ScreenRequest screenRequest) {
        Theater theater = theaterRepository.findById(screenRequest.getTheaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + screenRequest.getTheaterId()));

        validateTheaterOwnership(theater);

        if (!theater.getCity().isActive()) {
            throw new ResourceNotFoundException("Cannot add screen to theater '" + theater.getName() + 
                    "' because the city '" + theater.getCity().getName() + "' is currently inactive.");
        }

        if (!theater.isActive()) {
            throw new ResourceNotFoundException("Cannot add screen to theater '" + theater.getName() + "' because the theater is currently inactive.");
        }

        if (screenRepository.existsByNameIgnoreCaseAndTheaterId(screenRequest.getName(), screenRequest.getTheaterId())) {
            throw new ResourceAlreadyExistsException("Screen with name '" + screenRequest.getName() + 
                    "' already exists in theater " + theater.getName());
        }

        Screen screen = new Screen();
        screen.setName(screenRequest.getName());
        screen.setTheater(theater);
        screen.setActive(screenRequest.isActive());

        Screen savedScreen = screenRepository.save(screen);
        return mapToResponse(savedScreen);
    }

    @Override
    public ScreenResponse updateScreen(Long id, ScreenRequest screenRequest) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + id));

        Theater theater = theaterRepository.findById(screenRequest.getTheaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + screenRequest.getTheaterId()));

        validateTheaterOwnership(theater);

        if (!theater.getCity().isActive()) {
            throw new ResourceNotFoundException("Cannot update screen in theater '" + theater.getName() + 
                    "' because the city '" + theater.getCity().getName() + "' is currently inactive.");
        }

        if (!theater.isActive()) {
            throw new ResourceNotFoundException("Cannot update screen in theater '" + theater.getName() + "' because the theater is currently inactive.");
        }

        if (!screen.getName().equalsIgnoreCase(screenRequest.getName()) && 
                screenRepository.existsByNameIgnoreCaseAndTheaterId(screenRequest.getName(), screenRequest.getTheaterId())) {
            throw new ResourceAlreadyExistsException("Screen with name '" + screenRequest.getName() + 
                    "' already exists in theater " + theater.getName());
        }

        screen.setName(screenRequest.getName());
        screen.setTheater(theater);
        if (screenRequest.getIsActive() != null) {
            screen.setActive(screenRequest.getIsActive());
        }

        Screen updatedScreen = screenRepository.save(screen);
        return mapToResponse(updatedScreen);
    }

    @Override
    public void deleteScreen(Long id) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + id));
        
        validateTheaterOwnership(screen.getTheater());
        
        screen.setActive(false);
        screenRepository.save(screen);
    }

    private void validateTheaterOwnership(Theater theater) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTheaterOwner = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_THEATER_OWNER"));
        
        if (isAdmin) return;
        
        if (isTheaterOwner && theater.getOwner().getId().equals(currentUser.getId())) {
            return;
        }
        
        throw new AccessDeniedException("You do not have permission to modify screens for this theater.");
    }

    @Override
    public ScreenResponse getScreenById(Long id) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + id));
        return mapToResponse(screen);
    }

    @Override
    public List<ScreenResponse> getScreensByTheater(Long theaterId) {
        return screenRepository.findActiveScreensByTheaterId(theaterId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ScreenResponse mapToResponse(Screen screen) {
        return ScreenResponse.builder()
                .id(screen.getId())
                .name(screen.getName())
                .theaterId(screen.getTheater().getId())
                .theaterName(screen.getTheater().getName())
                .isActive(screen.isActive())
                .createdAt(screen.getCreatedAt())
                .updatedAt(screen.getUpdatedAt())
                .build();
    }
}
