package com.clone.BookMyShow.service;

import com.clone.BookMyShow.dto.MovieRequest;
import com.clone.BookMyShow.dto.MovieResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MovieService {
    MovieResponse addMovie(MovieRequest movieRequest, MultipartFile file);
    MovieResponse updateMovie(Long id, MovieRequest movieRequest, MultipartFile file);
    void deleteMovie(Long id);
    MovieResponse getMovieById(Long id);
    List<MovieResponse> getAllMovies();
    List<MovieResponse> getActiveMovies();
}
