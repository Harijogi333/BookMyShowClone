package com.clone.BookMyShow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CityRequest {
    @NotBlank(message = "City name is required")
    private String name;

    private boolean isActive = true;
}
