package com.clone.BookMyShow.controller;

import com.clone.BookMyShow.dto.SeatRequest;
import com.clone.BookMyShow.dto.SeatResponse;
import com.clone.BookMyShow.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<SeatResponse> addSeat(@Valid @RequestBody SeatRequest seatRequest) {
        return new ResponseEntity<>(seatService.addSeat(seatRequest), HttpStatus.CREATED);
    }

    @PostMapping("/bulk-add")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<List<SeatResponse>> addSeats(@Valid @RequestBody List<SeatRequest> seatRequests) {
        return new ResponseEntity<>(seatService.addSeats(seatRequests), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<SeatResponse> updateSeat(@PathVariable Long id, @Valid @RequestBody SeatRequest seatRequest) {
        return ResponseEntity.ok(seatService.updateSeat(id, seatRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatResponse> getSeatById(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getSeatById(id));
    }

    @GetMapping("/screen/{screenId}")
    public ResponseEntity<List<SeatResponse>> getSeatsByScreen(@PathVariable Long screenId) {
        return ResponseEntity.ok(seatService.getSeatsByScreen(screenId));
    }
}
