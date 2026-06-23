package com.clone.BookMyShow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long movieId;
    private String movieTitle;
    private Long screenId;
    private String screenName;
    private String theaterName;
    private String cityName;
    private Boolean isActive;
}
