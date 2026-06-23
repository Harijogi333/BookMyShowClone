package com.clone.BookMyShow.dto;

import com.clone.BookMyShow.entity.ShowSeatStatus;
import com.clone.BookMyShow.entity.SeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeatResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long seatId;
    private String seatNumber;
    private SeatType seatType;
    private Double price;
    private ShowSeatStatus status;
    private Boolean blockedByCurrentUser;
}
