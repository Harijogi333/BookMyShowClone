package com.clone.BookMyShow.service;

import com.clone.BookMyShow.dto.SeatRequest;
import com.clone.BookMyShow.dto.SeatResponse;

import java.util.List;

public interface SeatService {
    SeatResponse addSeat(SeatRequest seatRequest);
    List<SeatResponse> addSeats(List<SeatRequest> seatRequests); // Bulk addition
    SeatResponse updateSeat(Long id, SeatRequest seatRequest);
    void deleteSeat(Long id);
    SeatResponse getSeatById(Long id);
    List<SeatResponse> getSeatsByScreen(Long screenId);
}
