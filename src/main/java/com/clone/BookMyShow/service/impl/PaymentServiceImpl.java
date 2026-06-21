package com.clone.BookMyShow.service.impl;

import com.clone.BookMyShow.dto.PaymentInitiateRequest;
import com.clone.BookMyShow.dto.PaymentCompleteRequest;
import com.clone.BookMyShow.dto.PaymentResponse;
import com.clone.BookMyShow.entity.Booking;
import com.clone.BookMyShow.entity.BookingStatus;
import com.clone.BookMyShow.entity.Payment;
import com.clone.BookMyShow.entity.PaymentStatus;
import com.clone.BookMyShow.exception.ResourceNotFoundException;
import com.clone.BookMyShow.repository.BookingRepository;
import com.clone.BookMyShow.repository.PaymentRepository;
import com.clone.BookMyShow.security.CustomUserDetails;
import com.clone.BookMyShow.service.BookingService;
import com.clone.BookMyShow.service.PaymentService;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(PaymentInitiateRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));

        validateOwnership(booking);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Payment can only be initiated for PENDING bookings.");
        }

        if (Math.abs(booking.getTotalPrice() - request.getAmount()) > 0.01) {
            throw new IllegalArgumentException("Payment amount does not match booking total price.");
        }

        try {
            long amountInCents = Math.round(request.getAmount() * 100);
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("inr") // e.g. "usd" or "inr"
                    .putMetadata("booking_id", String.valueOf(booking.getId()))
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            Payment payment = Payment.builder()
                    .booking(booking)
                    .paymentMethod(request.getPaymentMethod())
                    .transactionId(intent.getId())
                    .amount(request.getAmount())
                    .status(PaymentStatus.PENDING)
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .paymentId(savedPayment.getId())
                    .bookingId(booking.getId())
                    .transactionId(intent.getId())
                    .amount(savedPayment.getAmount())
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(savedPayment.getPaymentMethod())
                    .paymentTime(savedPayment.getPaymentTime())
                    .message(intent.getClientSecret()) // Return Stripe client secret to the frontend
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate Stripe payment: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public PaymentResponse completePayment(PaymentCompleteRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found with id: " + request.getPaymentId()));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is already completed. Current status: " + payment.getStatus());
        }

        try {
            PaymentIntent intent = PaymentIntent.retrieve(payment.getTransactionId());
            String stripeStatus = intent.getStatus();

            String message;
            PaymentStatus finalStatus;

            if ("succeeded".equals(stripeStatus)) {
                finalStatus = PaymentStatus.SUCCESS;
                message = "Payment completed successfully.";
                bookingService.confirmBooking(payment.getBooking().getId());
            } else {
                finalStatus = PaymentStatus.FAILED;
                message = "Payment failed on Stripe. Status: " + stripeStatus;
                // Cancel booking and release seats immediately
                bookingService.cancelBooking(payment.getBooking().getId());
            }

            payment.setStatus(finalStatus);
            Payment savedPayment = paymentRepository.save(payment);

            return PaymentResponse.builder()
                    .paymentId(savedPayment.getId())
                    .bookingId(savedPayment.getBooking().getId())
                    .transactionId(savedPayment.getTransactionId())
                    .amount(savedPayment.getAmount())
                    .status(finalStatus)
                    .paymentMethod(savedPayment.getPaymentMethod())
                    .paymentTime(savedPayment.getPaymentTime())
                    .message(message)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to verify Stripe payment: " + e.getMessage(), e);
        }
    }

    private void validateOwnership(Booking booking) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !booking.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to pay for this booking.");
        }
    }
}
