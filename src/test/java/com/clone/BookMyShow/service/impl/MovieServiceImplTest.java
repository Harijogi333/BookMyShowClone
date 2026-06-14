package com.clone.BookMyShow.service.impl;

import com.clone.BookMyShow.dto.MovieRequest;
import com.clone.BookMyShow.dto.MovieResponse;
import com.clone.BookMyShow.entity.Genre;
import com.clone.BookMyShow.entity.Language;
import com.clone.BookMyShow.entity.Movie;
import com.clone.BookMyShow.exception.ResourceAlreadyExistsException;
import com.clone.BookMyShow.exception.ResourceNotFoundException;
import com.clone.BookMyShow.repository.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class MovieServiceImplTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieServiceImpl movieService;

    private Movie movie;
    private MovieRequest movieRequest;

    @BeforeEach
    void setUp() {
        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");
        movie.setLanguage(Language.ENGLISH);
        movie.setGenre(Genre.ACTION);
        movie.setDurationInMinutes(148);
        movie.setIsActive(true);
        movie.setCreatedAt(LocalDateTime.now());
        movie.setUpdatedAt(LocalDateTime.now());

        movieRequest = new MovieRequest();
        movieRequest.setTitle("Inception");
        movieRequest.setLanguage(Language.ENGLISH);
        movieRequest.setGenre(Genre.ACTION);
        movieRequest.setDurationInMinutes(148);
        movieRequest.setIsActive(true);
    }

    @Test
    @DisplayName("Should successfully add a movie")
    void addMovie_Success() {
        log.info("Testing addMovie success case");
        when(movieRepository.findByTitleAndLanguage(anyString(), any(Language.class))).thenReturn(Optional.empty());
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        MovieResponse response = movieService.addMovie(movieRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(movieRequest.getTitle());
        
        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(movieCaptor.capture());
        assertThat(movieCaptor.getValue().getTitle()).isEqualTo(movieRequest.getTitle());
    }

    @Test
    @DisplayName("Should throw ResourceAlreadyExistsException when adding a duplicate movie")
    void addMovie_Failure_AlreadyExists() {
        log.info("Testing addMovie failure case: already exists");
        when(movieRepository.findByTitleAndLanguage(anyString(), any(Language.class))).thenReturn(Optional.of(movie));

        assertThatThrownBy(() -> movieService.addMovie(movieRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should successfully update a movie")
    void updateMovie_Success() {
        log.info("Testing updateMovie success case");
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.findByTitleAndLanguage(anyString(), any(Language.class))).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        MovieResponse response = movieService.updateMovie(1L, movieRequest);

        assertThat(response).isNotNull();
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    @DisplayName("Should successfully delete (deactivate) a movie")
    void deleteMovie_Success() {
        log.info("Testing deleteMovie success case");
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        movieService.deleteMovie(1L);

        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(movieCaptor.capture());
        assertThat(movieCaptor.getValue().getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should successfully get movie by ID")
    void getMovieById_Success() {
        log.info("Testing getMovieById success case");
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        MovieResponse response = movieService.getMovieById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should successfully get all movies")
    void getAllMovies_Success() {
        log.info("Testing getAllMovies success case");
        when(movieRepository.findAll()).thenReturn(Collections.singletonList(movie));

        List<MovieResponse> responses = movieService.getAllMovies();

        assertThat(responses).hasSize(1);
    }
}
