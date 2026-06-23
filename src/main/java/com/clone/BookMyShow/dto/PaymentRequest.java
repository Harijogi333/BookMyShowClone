package com.clone.BookMyShow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotNull(message = "Amount is required")
    private Double amount;

    @NotNull(message = "card number is mandatory")
    private String cardNumber;

    @NotNull(message = "expiry date is required")
    private String expiryDate;

    @NotNull(message = "cvv is required")
    private String cvv;
}
