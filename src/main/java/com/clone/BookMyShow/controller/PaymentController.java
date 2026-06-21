package com.clone.BookMyShow.controller;

import com.clone.BookMyShow.dto.PaymentInitiateRequest;
import com.clone.BookMyShow.dto.PaymentCompleteRequest;
import com.clone.BookMyShow.dto.PaymentResponse;
import com.clone.BookMyShow.entity.PaymentStatus;
import com.clone.BookMyShow.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentInitiateRequest request) {
        PaymentResponse response = paymentService.initiatePayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/complete")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'THEATER_OWNER')")
    public ResponseEntity<PaymentResponse> completePayment(@Valid @RequestBody PaymentCompleteRequest request) {
        PaymentResponse response = paymentService.completePayment(request);
        
        if (response.getStatus() == PaymentStatus.SUCCESS) {
            return ResponseEntity.ok(response);
        } else {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
