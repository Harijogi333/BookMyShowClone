package com.clone.BookMyShow.dto;

import com.clone.BookMyShow.entity.SeatType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ShowRequest {
    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotNull(message = "Screen ID is required")
    private Long screenId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "Price map is required")
    private Map<SeatType, Double> seatTypePrices;

    private Boolean isActive;
}
