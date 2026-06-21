package com.clone.BookMyShow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // e.g. "UPI", "CARD", "NET_BANKING"

    @NotNull(message = "Amount is required")
    private Double amount;

    // Optional fields for dummy card payment mock behavior
    private String cardNumber;
    private String expiryDate;
    private String cvv;
}
