package com.clone.BookMyShow.repository;

import com.clone.BookMyShow.entity.ShowSeat;
import com.clone.BookMyShow.entity.ShowSeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {
    List<ShowSeat> findByShowId(Long showId);

    boolean existsByShowIdAndStatusIn(Long showId, Collection<ShowSeatStatus> statuses);

    @Modifying
    @Query("DELETE FROM ShowSeat ss WHERE ss.show.id = :showId")
    void deleteByShowId(@Param("showId") Long showId);

    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = :status WHERE ss.show.id = :showId AND ss.status IN :currentStatuses")
    void updateStatusByShowId(@Param("showId") Long showId, 
                             @Param("status") ShowSeatStatus status, 
                             @Param("currentStatuses") Collection<ShowSeatStatus> currentStatuses);
}
