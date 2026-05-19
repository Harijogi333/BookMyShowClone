package com.clone.BookMyShow.dto;

import com.clone.BookMyShow.entity.Genre;
import com.clone.BookMyShow.entity.Language;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MovieRequest {
    @NotBlank(message = "Movie title is required")
    private String title;

    private String description;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationInMinutes;

    @NotNull(message = "Language is required")
    private Language language;

    @NotNull(message = "Genre is required")
    private Genre genre;

    private Double rating;

    private Boolean isActive;
}
