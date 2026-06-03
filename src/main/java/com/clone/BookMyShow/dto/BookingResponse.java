package com.clone.BookMyShow.dto;

import com.clone.BookMyShow.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private String userName;
    private String movieTitle;
    private String theaterName;
    private String screenName;
    private LocalDateTime startTime;
    private List<String> seatNumbers;
    private Double totalPrice;
    private BookingStatus status;
    private LocalDateTime bookingTime;
}
