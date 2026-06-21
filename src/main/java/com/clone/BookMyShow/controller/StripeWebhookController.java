package com.clone.BookMyShow.controller;

import com.clone.BookMyShow.entity.Payment;
import com.clone.BookMyShow.entity.PaymentStatus;
import com.clone.BookMyShow.repository.PaymentRepository;
import com.clone.BookMyShow.service.BookingService;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/webhooks")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    @Value("${stripe.api.webhook-secret}")
    private String endpointSecret;

    @PostMapping(produces = "application/json")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {
            // 1. Verify the event is genuinely from Stripe
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            log.info("Received Stripe Webhook Event: {}", event.getType());

            if ("payment_intent.succeeded".equals(event.getType())) {
                // Use deserializeUnsafe() to bypass API version mismatch issues
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().deserializeUnsafe();
                log.info("Processing webhook for successful PaymentIntent ID: {}", intent.getId());

                // Gracefully handle unknown payment intents (e.g. from generic stripe trigger commands)
                Payment payment = paymentRepository.findByTransactionId(intent.getId()).orElse(null);
                if (payment == null) {
                    log.warn("No matching payment record found for transactionId: {}. Ignoring event.", intent.getId());
                    return ResponseEntity.ok("Event ignored: No matching payment record.");
                }

                if (payment.getStatus() == PaymentStatus.PENDING) {
                    payment.setStatus(PaymentStatus.SUCCESS);
                    paymentRepository.save(payment);
                    bookingService.confirmBooking(payment.getBooking().getId());
                    log.info("Booking confirmed via webhook for ID: {}", payment.getBooking().getId());
                }

            } else if ("payment_intent.payment_failed".equals(event.getType())) {
                // Use deserializeUnsafe() to bypass API version mismatch issues
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().deserializeUnsafe();
                log.warn("Processing webhook for failed PaymentIntent ID: {} with error: {}",
                        intent.getId(),
                        intent.getLastPaymentError() != null ? intent.getLastPaymentError().getMessage() : "Unknown error");

                // Gracefully handle unknown payment intents
                Payment payment = paymentRepository.findByTransactionId(intent.getId()).orElse(null);
                if (payment == null) {
                    log.warn("No matching payment record found for transactionId: {}. Ignoring event.", intent.getId());
                    return ResponseEntity.ok("Event ignored: No matching payment record.");
                }

                if (payment.getStatus() == PaymentStatus.PENDING) {
                    payment.setStatus(PaymentStatus.FAILED);
                    paymentRepository.save(payment);
                    bookingService.cancelBooking(payment.getBooking().getId());
                    log.warn("Booking cancelled and seats released via webhook for failed payment ID: {}", payment.getBooking().getId());
                }
            }

            return ResponseEntity.ok("Webhook Handled Successfully");

        } catch (Exception e) {
            log.error("Error processing Stripe webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook Error: " + e.getMessage());
        }
    }
}
