package com.clone.BookMyShow.service;

import com.clone.BookMyShow.dto.MovieRequest;
import com.clone.BookMyShow.dto.MovieResponse;

import java.util.List;

public interface MovieService {
    MovieResponse addMovie(MovieRequest movieRequest);
    MovieResponse updateMovie(Long id, MovieRequest movieRequest);
    void deleteMovie(Long id);
    MovieResponse getMovieById(Long id);
    List<MovieResponse> getAllMovies();
    List<MovieResponse> getActiveMovies();
}
