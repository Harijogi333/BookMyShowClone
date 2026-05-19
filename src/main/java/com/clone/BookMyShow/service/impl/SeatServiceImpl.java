package com.clone.BookMyShow.service.impl;

import com.clone.BookMyShow.dto.SeatRequest;
import com.clone.BookMyShow.dto.SeatResponse;
import com.clone.BookMyShow.entity.Screen;
import com.clone.BookMyShow.entity.Seat;
import com.clone.BookMyShow.entity.Theater;
import com.clone.BookMyShow.exception.ResourceAlreadyExistsException;
import com.clone.BookMyShow.exception.ResourceNotFoundException;
import com.clone.BookMyShow.repository.ScreenRepository;
import com.clone.BookMyShow.repository.SeatRepository;
import com.clone.BookMyShow.security.CustomUserDetails;
import com.clone.BookMyShow.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;

    @Override
    @Transactional
    public SeatResponse addSeat(SeatRequest seatRequest) {
        Screen screen = screenRepository.findById(seatRequest.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + seatRequest.getScreenId()));

        validateTheaterOwnership(screen.getTheater());
        validateHierarchyStatus(screen);

        if (seatRepository.existsBySeatNumberAndScreenId(seatRequest.getSeatNumber(), seatRequest.getScreenId())) {
            throw new ResourceAlreadyExistsException("Seat '" + seatRequest.getSeatNumber() + "' already exists in this screen.");
        }

        Seat seat = new Seat();
        seat.setSeatNumber(seatRequest.getSeatNumber());
        seat.setSeatType(seatRequest.getSeatType());
        seat.setScreen(screen);
        seat.setIsActive(seatRequest.getIsActive() != null ? seatRequest.getIsActive() : true);

        Seat savedSeat = seatRepository.save(seat);
        return mapToResponse(savedSeat);
    }

    @Override
    @Transactional
    public List<SeatResponse> addSeats(List<SeatRequest> seatRequests) {
        return seatRequests.stream()
                .map(this::addSeat)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SeatResponse updateSeat(Long id, SeatRequest seatRequest) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));

        validateTheaterOwnership(seat.getScreen().getTheater());

        if (!seat.getIsActive() && (seatRequest.getIsActive() == null || !seatRequest.getIsActive())) {
            throw new ResourceNotFoundException("Cannot update seat '" + seat.getSeatNumber() + "' because it is currently inactive. Please set isActive to true to reactivate it.");
        }

        Screen screen = screenRepository.findById(seatRequest.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + seatRequest.getScreenId()));

        validateHierarchyStatus(screen);

        if (!seat.getSeatNumber().equalsIgnoreCase(seatRequest.getSeatNumber()) && 
                seatRepository.existsBySeatNumberAndScreenId(seatRequest.getSeatNumber(), seatRequest.getScreenId())) {
            throw new ResourceAlreadyExistsException("Seat '" + seatRequest.getSeatNumber() + "' already exists in this screen.");
        }

        seat.setSeatNumber(seatRequest.getSeatNumber());
        seat.setSeatType(seatRequest.getSeatType());
        seat.setScreen(screen);
        if (seatRequest.getIsActive() != null) {
            seat.setIsActive(seatRequest.getIsActive());
        }

        Seat updatedSeat = seatRepository.save(seat);
        return mapToResponse(updatedSeat);
    }

    @Override
    @Transactional
    public void deleteSeat(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));
        
        validateTheaterOwnership(seat.getScreen().getTheater());
        
        seat.setIsActive(false);
        seatRepository.save(seat);
    }

    @Override
    public SeatResponse getSeatById(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));
        return mapToResponse(seat);
    }

    @Override
    public List<SeatResponse> getSeatsByScreen(Long screenId) {
        return seatRepository.findActiveSeatsByScreenId(screenId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateTheaterOwnership(Theater theater) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTheaterOwner = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_THEATER_OWNER"));
        
        if (isAdmin) return;
        
        if (isTheaterOwner && theater.getOwner().getId().equals(currentUser.getId())) {
            return;
        }
        
        throw new AccessDeniedException("You do not have permission to modify seats for this theater.");
    }

    private void validateHierarchyStatus(Screen screen) {
        if (!screen.getTheater().getCity().getIsActive()) {
            throw new ResourceNotFoundException("Cannot manage seats because the city '" + screen.getTheater().getCity().getName() + "' is currently inactive.");
        }
        if (!screen.getTheater().getIsActive()) {
            throw new ResourceNotFoundException("Cannot manage seats because the theater '" + screen.getTheater().getName() + "' is currently inactive.");
        }
        if (!screen.getIsActive()) {
            throw new ResourceNotFoundException("Cannot manage seats because the screen '" + screen.getName() + "' is currently inactive.");
        }
    }

    private SeatResponse mapToResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .seatType(seat.getSeatType())
                .screenId(seat.getScreen().getId())
                .screenName(seat.getScreen().getName())
                .isActive(seat.getIsActive())
                .createdAt(seat.getCreatedAt())
                .updatedAt(seat.getUpdatedAt())
                .build();
    }
}
