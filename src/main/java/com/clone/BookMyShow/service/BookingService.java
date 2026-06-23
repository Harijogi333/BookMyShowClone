package com.clone.BookMyShow.service;

import com.clone.BookMyShow.dto.BookingRequest;
import com.clone.BookMyShow.dto.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingResponse createBooking(BookingRequest bookingRequest);
    BookingResponse confirmBooking(Long bookingId);
    BookingResponse getBookingById(Long id);
    Page<BookingResponse> getUserBookings(Long userId, Pageable pageable);
    void cancelBooking(Long id);
}
