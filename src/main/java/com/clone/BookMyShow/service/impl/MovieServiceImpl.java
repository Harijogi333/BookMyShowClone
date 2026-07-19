package com.clone.BookMyShow.service.impl;

import com.clone.BookMyShow.dto.MovieRequest;
import com.clone.BookMyShow.dto.MovieResponse;
import com.clone.BookMyShow.entity.Movie;
import com.clone.BookMyShow.exception.ResourceAlreadyExistsException;
import com.clone.BookMyShow.exception.ResourceNotFoundException;
import com.clone.BookMyShow.repository.MovieRepository;
import com.clone.BookMyShow.service.CloudinaryService;
import com.clone.BookMyShow.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @CacheEvict(value = "movies", allEntries = true)
    public MovieResponse addMovie(MovieRequest movieRequest, @Nullable MultipartFile file) {
        movieRepository.findByTitleAndLanguage(movieRequest.getTitle(), movieRequest.getLanguage())
                .ifPresent(M -> {
                    throw new ResourceAlreadyExistsException("Movie '" + movieRequest.getTitle() + "' in " + movieRequest.getLanguage() + " already exists.");
                });

        Movie movie = new Movie();
        updateMovieFields(movie, movieRequest);
        movie.setIsActive(movieRequest.getIsActive() != null ? movieRequest.getIsActive() : true);
        
        Movie savedMovie = movieRepository.save(movie);
        savedMovie = uploadImageIfPresent(savedMovie, file);
        return mapToResponse(savedMovie);
    }

    @Override
    @CacheEvict(value = "movies", allEntries = true)
    public MovieResponse updateMovie(Long id, MovieRequest movieRequest, @Nullable MultipartFile file) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        movieRepository.findByTitleAndLanguage(movieRequest.getTitle(), movieRequest.getLanguage())
                .ifPresent(m -> {
                    if (!m.getId().equals(id)) {
                        throw new ResourceAlreadyExistsException("Movie '" + movieRequest.getTitle() + "' in " + movieRequest.getLanguage() + " already exists.");
                    }
                });

        updateMovieFields(movie, movieRequest);
        if (movieRequest.getIsActive() != null) {
            movie.setIsActive(movieRequest.getIsActive());
        }

        Movie updatedMovie = movieRepository.save(movie);
        updatedMovie = uploadImageIfPresent(updatedMovie, file);
        return mapToResponse(updatedMovie);
    }

    @Override
    @CacheEvict(value = "movies", allEntries = true)
    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        movie.setIsActive(false);
        movieRepository.save(movie);
    }

    @Override
    @Cacheable(value = "movies", key = "#id")
    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
        return mapToResponse(movie);
    }

    @Override
    @Cacheable(value = "movies", key = "'all'")
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "movies", key = "'active'")
    public List<MovieResponse> getActiveMovies() {
        return movieRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Movie uploadImageIfPresent(Movie movie, @Nullable MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                if (movie.getCloudinaryPublicId() != null) {
                    cloudinaryService.deleteImage(movie.getCloudinaryPublicId());
                }
                Map result = cloudinaryService.uploadImage(file);
                movie.setImageUrl((String) result.get("secure_url"));
                movie.setCloudinaryPublicId((String) result.get("public_id"));
                movie = movieRepository.save(movie);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image to Cloudinary", e);
            }
        }
        return movie;
    }

    private void updateMovieFields(Movie movie, MovieRequest request) {
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDurationInMinutes(request.getDurationInMinutes());
        movie.setLanguage(request.getLanguage());
        movie.setGenre(request.getGenre());
        movie.setRating(request.getRating());
    }

    private MovieResponse mapToResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .durationInMinutes(movie.getDurationInMinutes())
                .language(movie.getLanguage())
                .genre(movie.getGenre())
                .rating(movie.getRating())
                .isActive(movie.getIsActive())
                .imageUrl(movie.getImageUrl())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .build();
    }
}
