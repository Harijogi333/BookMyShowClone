package com.clone.BookMyShow.repository;

import com.clone.BookMyShow.entity.Booking;
import com.clone.BookMyShow.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByStatusAndBookingTimeBefore(BookingStatus status, LocalDateTime time);

    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.showSeats ss " +
           "LEFT JOIN FETCH ss.seat " +
           "JOIN FETCH b.show s " +
           "JOIN FETCH s.movie " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.theater " +
           "WHERE b.id = :id")
    Optional<Booking> findByIdWithHierarchy(@Param("id") Long id);

    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.showSeats ss " +
           "LEFT JOIN FETCH ss.seat " +
           "JOIN FETCH b.show s " +
           "JOIN FETCH s.movie " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.theater " +
           "WHERE b.user.id = :userId")
    List<Booking> findByUserIdWithHierarchy(@Param("userId") Long userId);

    @Query(value = "SELECT b FROM Booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.show s " +
           "JOIN FETCH s.movie " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.theater t " +
           "JOIN FETCH t.city " +
           "WHERE b.user.id = :userId "+
            "AND b.status='CONFIRMED' ",
           countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId")
    Page<Booking> findPaginatedByUserId(@Param("userId") Long userId, Pageable pageable);
}
