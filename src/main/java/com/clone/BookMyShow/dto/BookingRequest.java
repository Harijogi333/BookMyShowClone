package com.clone.BookMyShow.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BookingRequest {
    @NotEmpty(message = "At least one seat must be selected")
    private List<Long> showSeatIds;
}
