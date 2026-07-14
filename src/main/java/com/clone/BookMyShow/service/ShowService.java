package com.clone.BookMyShow.service;

import com.clone.BookMyShow.dto.ShowRequest;
import com.clone.BookMyShow.dto.ShowResponse;
import com.clone.BookMyShow.dto.ShowSeatResponse;

import java.util.List;
import java.util.Map;

public interface ShowService {
    ShowResponse addShow(ShowRequest showRequest);
    ShowResponse updateShow(Long id, ShowRequest showRequest);
    void deleteShow(Long id);
    ShowResponse getShowById(Long id);
    List<ShowResponse> getShowsByScreen(Long screenId);
    List<ShowResponse> getShowsByTheater(Long theaterId);
    Map<Long,List<ShowResponse>> getShowsByMovie(Long movieId);
    List<ShowResponse> getShowsByCity(Long cityId);
    Map<Long,List<ShowResponse>> getShowsByMovieAndCity(Long movieId, Long cityId);
    List<ShowResponse> getShowsByMovieAndDate(Long movieId, java.time.LocalDate date);
    Map<Long,List<ShowResponse>> getShowsByMovieAndCityAndDate(Long movieId, Long cityId, java.time.LocalDate date);
    List<ShowResponse> getActiveShows();
    List<ShowSeatResponse> getShowSeats(Long showId);
}
