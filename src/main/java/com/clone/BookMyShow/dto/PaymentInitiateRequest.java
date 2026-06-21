package com.clone.BookMyShow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentInitiateRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // e.g. "STRIPE"

    @NotNull(message = "Amount is required")
    private Double amount;
}
