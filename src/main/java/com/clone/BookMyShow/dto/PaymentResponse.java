package com.clone.BookMyShow.dto;

import com.clone.BookMyShow.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long paymentId;
    private Long bookingId;
    private String transactionId;
    private Double amount;
    private PaymentStatus status;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private String message;
}
