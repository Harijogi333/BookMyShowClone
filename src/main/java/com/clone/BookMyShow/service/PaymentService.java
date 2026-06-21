package com.clone.BookMyShow.service;

import com.clone.BookMyShow.dto.PaymentInitiateRequest;
import com.clone.BookMyShow.dto.PaymentCompleteRequest;
import com.clone.BookMyShow.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse initiatePayment(PaymentInitiateRequest request);
    PaymentResponse completePayment(PaymentCompleteRequest request);
}
