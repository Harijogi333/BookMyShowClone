package com.clone.BookMyShow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @NotNull(message = "Booking is required")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Booking booking;

    @NotNull(message = "Payment method is required")
    private String paymentMethod; // e.g., UPI, CARD, WALLET

    @Column(unique = true, nullable = false)
    private String transactionId; // Unique UUID for tracking

    @NotNull(message = "Amount is required")
    private Double amount;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Payment status is required")
    private PaymentStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime paymentTime;
}
