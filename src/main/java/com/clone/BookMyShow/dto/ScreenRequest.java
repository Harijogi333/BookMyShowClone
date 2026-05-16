package com.clone.BookMyShow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScreenRequest {
    @NotBlank(message = "Screen name is required")
    private String name;

    @NotNull(message = "Theater ID is required")
    private Long theaterId;

    private Boolean isActive;
}
