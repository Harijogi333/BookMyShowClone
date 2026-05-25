package com.clone.BookMyShow.service;

import com.clone.BookMyShow.dto.ShowRequest;
import com.clone.BookMyShow.dto.ShowResponse;

import java.util.List;

public interface ShowService {
    ShowResponse addShow(ShowRequest showRequest);
    ShowResponse updateShow(Long id, ShowRequest showRequest);
    void deleteShow(Long id);
    ShowResponse getShowById(Long id);
    List<ShowResponse> getShowsByScreen(Long screenId);
    List<ShowResponse> getShowsByTheater(Long theaterId);
    List<ShowResponse> getShowsByMovie(Long movieId);
    List<ShowResponse> getShowsByCity(Long cityId);
    List<ShowResponse> getShowsByMovieAndCity(Long movieId, Long cityId);
    List<ShowResponse> getShowsByMovieAndDate(Long movieId, java.time.LocalDate date);
    List<ShowResponse> getActiveShows();
}
