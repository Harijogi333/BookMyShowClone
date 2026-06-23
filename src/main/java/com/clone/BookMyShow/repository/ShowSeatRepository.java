package com.clone.BookMyShow.repository;

import com.clone.BookMyShow.entity.ShowSeat;
import com.clone.BookMyShow.entity.ShowSeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ss FROM ShowSeat ss WHERE ss.id IN :ids")
    List<ShowSeat> findByIdInWithLock(@Param("ids") List<Long> ids);

    @Query("SELECT ss FROM ShowSeat ss JOIN FETCH ss.seat s WHERE ss.show.id = :showId")
    List<ShowSeat> findByShowIdWithSeat(@Param("showId") Long showId);

    List<ShowSeat> findByShowId(Long showId);

    boolean existsByShowIdAndStatusIn(Long showId, Collection<ShowSeatStatus> statuses);

    @Modifying
    @Query("DELETE FROM ShowSeat ss WHERE ss.show.id = :showId")
    void deleteByShowId(@Param("showId") Long showId);

    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = :status, ss.blockedAt = null WHERE ss.booking.id = :bookingId")
    void updateStatusByBookingId(@Param("bookingId") Long bookingId, @Param("status") ShowSeatStatus status);

    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = :status, ss.blockedAt = null, ss.booking = null WHERE ss.booking.id = :bookingId")
    void releaseSeatsByBookingId(@Param("bookingId") Long bookingId, @Param("status") ShowSeatStatus status);

    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = :status WHERE ss.show.id = :showId AND ss.status IN :currentStatuses")
    void updateStatusByShowId(@Param("showId") Long showId, 
                             @Param("status") ShowSeatStatus status, 
                             @Param("currentStatuses") Collection<ShowSeatStatus> currentStatuses);

    @Query("SELECT ss FROM ShowSeat ss JOIN FETCH ss.seat WHERE ss.booking.id IN :bookingIds")
    List<ShowSeat> findByBookingIdInWithSeat(@Param("bookingIds") List<Long> bookingIds);
    }
