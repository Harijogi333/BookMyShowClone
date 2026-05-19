package com.clone.BookMyShow.repository;

import com.clone.BookMyShow.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    @Query("SELECT s FROM Seat s JOIN s.screen sc JOIN sc.theater t JOIN t.city c " +
           "WHERE sc.id = :screenId AND s.isActive = true AND sc.isActive = true " +
           "AND t.isActive = true AND c.isActive = true")
    List<Seat> findActiveSeatsByScreenId(Long screenId);

    List<Seat> findByScreenId(Long screenId);
    
    boolean existsBySeatNumberAndScreenId(String seatNumber, Long screenId);
}
