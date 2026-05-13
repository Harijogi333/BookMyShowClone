package com.clone.BookMyShow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "show_seats", uniqueConstraints = @UniqueConstraint(columnNames = {"show_id", "seat_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false)
    @NotNull(message = "Show is required")
    private Show show;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    @NotNull(message = "Seat is required")
    private Seat seat;

    @PositiveOrZero(message = "Price cannot be negative")
    @NotNull(message = "Price is required")
    private Double price;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    private ShowSeatStatus status = ShowSeatStatus.AVAILABLE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
