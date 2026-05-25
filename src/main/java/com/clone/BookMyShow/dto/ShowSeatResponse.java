package com.clone.BookMyShow.dto;

import com.clone.BookMyShow.entity.ShowSeatStatus;
import com.clone.BookMyShow.entity.SeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeatResponse {
    private Long id;
    private Long seatId;
    private String seatNumber;
    private SeatType seatType;
    private Double price;
    private ShowSeatStatus status;
}
