package com.clone.BookMyShow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentCompleteRequest {
    @NotNull(message = "Payment ID is required")
    private Long paymentId;
}
