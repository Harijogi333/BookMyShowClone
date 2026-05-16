package com.clone.BookMyShow.controller;

import com.clone.BookMyShow.dto.ScreenRequest;
import com.clone.BookMyShow.dto.ScreenResponse;
import com.clone.BookMyShow.service.ScreenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/screens")
@RequiredArgsConstructor
public class ScreenController {

    private final ScreenService screenService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<ScreenResponse> addScreen(@Valid @RequestBody ScreenRequest screenRequest) {
        return new ResponseEntity<>(screenService.addScreen(screenRequest), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<ScreenResponse> updateScreen(@PathVariable Long id, @Valid @RequestBody ScreenRequest screenRequest) {
        return ResponseEntity.ok(screenService.updateScreen(id, screenRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<Void> deleteScreen(@PathVariable Long id) {
        screenService.deleteScreen(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScreenResponse> getScreenById(@PathVariable Long id) {
        return ResponseEntity.ok(screenService.getScreenById(id));
    }

    @GetMapping("/theater/{theaterId}")
    public ResponseEntity<List<ScreenResponse>> getScreensByTheater(@PathVariable Long theaterId) {
        return ResponseEntity.ok(screenService.getScreensByTheater(theaterId));
    }
}
