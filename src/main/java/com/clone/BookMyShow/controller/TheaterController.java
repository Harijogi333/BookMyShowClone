package com.clone.BookMyShow.controller;

import com.clone.BookMyShow.dto.TheaterRequest;
import com.clone.BookMyShow.dto.TheaterResponse;
import com.clone.BookMyShow.service.TheaterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/theaters")
@RequiredArgsConstructor
public class TheaterController {

    private final TheaterService theaterService;

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TheaterResponse> addTheater(@Valid @RequestBody TheaterRequest theaterRequest) {
        return new ResponseEntity<>(theaterService.addTheater(theaterRequest), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TheaterResponse> updateTheater(@PathVariable Long id, @Valid @RequestBody TheaterRequest theaterRequest) {
        return ResponseEntity.ok(theaterService.updateTheater(id, theaterRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTheater(@PathVariable Long id) {
        theaterService.deleteTheater(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TheaterResponse> getTheaterById(@PathVariable Long id) {
        return ResponseEntity.ok(theaterService.getTheaterById(id));
    }

    @GetMapping
    public ResponseEntity<List<TheaterResponse>> getAllTheaters() {
        return ResponseEntity.ok(theaterService.getAllTheaters());
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<TheaterResponse>> getTheatersByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(theaterService.getTheatersByCity(cityId));
    }
}
