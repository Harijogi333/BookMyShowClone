package com.clone.BookMyShow.controller;

import com.clone.BookMyShow.dto.ShowRequest;
import com.clone.BookMyShow.dto.ShowResponse;
import com.clone.BookMyShow.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.clone.BookMyShow.dto.ShowSeatResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<ShowResponse> addShow(@Valid @RequestBody ShowRequest showRequest) {
        return new ResponseEntity<>(showService.addShow(showRequest), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<ShowResponse> updateShow(@PathVariable Long id, @Valid @RequestBody ShowRequest showRequest) {
        return ResponseEntity.ok(showService.updateShow(id, showRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<Void> deleteShow(@PathVariable Long id) {
        showService.deleteShow(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowResponse> getShowById(@PathVariable Long id) {
        return ResponseEntity.ok(showService.getShowById(id));
    }

    @GetMapping("/screen/{screenId}")
    public ResponseEntity<List<ShowResponse>> getShowsByScreen(@PathVariable Long screenId) {
        return ResponseEntity.ok(showService.getShowsByScreen(screenId));
    }

    @GetMapping("/theater/{theaterId}")
    public ResponseEntity<List<ShowResponse>> getShowsByTheater(@PathVariable Long theaterId) {
        return ResponseEntity.ok(showService.getShowsByTheater(theaterId));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowResponse>> getShowsByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(showService.getShowsByMovie(movieId));
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<ShowResponse>> getShowsByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(showService.getShowsByCity(cityId));
    }

    @GetMapping("/movie/{movieId}/city/{cityId}")
    public ResponseEntity<List<ShowResponse>> getShowsByMovieAndCity(
            @PathVariable Long movieId, 
            @PathVariable Long cityId) {
        return ResponseEntity.ok(showService.getShowsByMovieAndCity(movieId, cityId));
    }

    @GetMapping("/movie/{movieId}/date/{date}")
    public ResponseEntity<List<ShowResponse>> getShowsByMovieAndDate(
            @PathVariable Long movieId, 
            @PathVariable @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        return ResponseEntity.ok(showService.getShowsByMovieAndDate(movieId, date));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ShowResponse>> getActiveShows() {
        return ResponseEntity.ok(showService.getActiveShows());
    }

    @GetMapping("/{showId}/seats")
    public ResponseEntity<List<ShowSeatResponse>> getShowSeats(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getShowSeats(showId));
    }
}
