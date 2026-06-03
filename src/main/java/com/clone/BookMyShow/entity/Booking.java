package com.clone.BookMyShow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false)
    @NotNull(message = "Show is required")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Show show;

    @OneToMany(mappedBy = "booking")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ShowSeat> showSeats;

    @NotNull(message = "Total price is required")
    @PositiveOrZero(message = "Total price cannot be negative")
    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Booking status is required")
    private BookingStatus status = BookingStatus.PENDING;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime bookingTime;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
