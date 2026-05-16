package com.clone.BookMyShow.service;

import com.clone.BookMyShow.dto.ScreenRequest;
import com.clone.BookMyShow.dto.ScreenResponse;

import java.util.List;

public interface ScreenService {
    ScreenResponse addScreen(ScreenRequest screenRequest);
    ScreenResponse updateScreen(Long id, ScreenRequest screenRequest);
    void deleteScreen(Long id); // Soft delete
    ScreenResponse getScreenById(Long id);
    List<ScreenResponse> getScreensByTheater(Long theaterId);
}
