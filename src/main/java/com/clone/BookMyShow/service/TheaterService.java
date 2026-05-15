package com.clone.BookMyShow.service;

import com.clone.BookMyShow.dto.TheaterRequest;
import com.clone.BookMyShow.dto.TheaterResponse;

import java.util.List;

public interface TheaterService {
    TheaterResponse addTheater(TheaterRequest theaterRequest);
    TheaterResponse updateTheater(Long id, TheaterRequest theaterRequest);
    void deleteTheater(Long id); // Soft delete
    TheaterResponse getTheaterById(Long id);
    List<TheaterResponse> getAllTheaters();
    List<TheaterResponse> getTheatersByCity(Long cityId);
}
