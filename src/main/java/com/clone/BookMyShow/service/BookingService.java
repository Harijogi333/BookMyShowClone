package com.clone.BookMyShow.service;

import com.clone.BookMyShow.dto.BookingRequest;
import com.clone.BookMyShow.dto.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest bookingRequest);
    BookingResponse confirmBooking(Long bookingId);
    BookingResponse getBookingById(Long id);
    List<BookingResponse> getUserBookings(Long userId);
    void cancelBooking(Long id);
}
