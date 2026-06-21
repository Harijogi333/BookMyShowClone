package com.clone.BookMyShow.controller;

import com.clone.BookMyShow.dto.BookingRequest;
import com.clone.BookMyShow.dto.BookingResponse;
import com.clone.BookMyShow.security.CustomUserDetails;
import com.clone.BookMyShow.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping()
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        return new ResponseEntity<>(bookingService.createBooking(bookingRequest), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(bookingService.getUserBookings(currentUser.getId()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}
