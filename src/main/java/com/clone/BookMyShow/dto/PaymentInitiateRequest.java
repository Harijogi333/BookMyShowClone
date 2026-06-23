package com.clone.BookMyShow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PaymentInitiateRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^CARD$", message = "Only CARD payment method is supported")
    private String paymentMethod;

    @NotNull(message = "Amount is required")
    private Double amount;
}
