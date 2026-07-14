package com.clone.BookMyShow.repository;

import com.clone.BookMyShow.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
    
    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.theater t " +
           "JOIN FETCH t.owner o " +
           "JOIN FETCH t.city c " +
           "JOIN FETCH s.movie m " +
           "WHERE s.id = :id")
    java.util.Optional<Show> findByIdWithHierarchy(@Param("id") Long id);

    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.theater t " +
           "JOIN FETCH t.city c " +
           "JOIN FETCH s.movie m " +
           "WHERE s.isActive = true " +
           "AND sc.isActive = true " +
           "AND t.isActive = true " +
           "AND c.isActive = true " +
           "AND m.isActive = true")
    List<Show> findByIsActiveTrue();

    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.theater t " +
           "JOIN FETCH t.city c " +
           "JOIN FETCH s.movie m " +
           "WHERE s.screen.id = :screenId " +
           "AND s.isActive = true " +
           "AND sc.isActive = true " +
           "AND t.isActive = true " +
           "AND c.isActive = true " +
           "AND m.isActive = true")
    List<Show> findByScreenId(@Param("screenId") Long screenId);

    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.theater t " +
           "JOIN FETCH t.city c " +
           "JOIN FETCH s.movie m " +
           "WHERE s.screen.theater.id = :theaterId " +
           "AND s.isActive = true " +
           "AND sc.isActive = true " +
           "AND t.isActive = true " +
           "AND c.isActive = true " +
           "AND m.isActive = true")
    List<Show> findByTheaterId(@Param("theaterId") Long theaterId);

    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.theater t " +
           "JOIN FETCH t.city c " +
           "JOIN FETCH s.movie m " +
           "WHERE s.movie.id = :movieId " +
           "AND s.isActive = true " +
           "AND sc.isActive = true " +
           "AND t.isActive = true " +
           "AND c.isActive = true " +
           "AND m.isActive = true")
    List<Show> findByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.theater t " +
           "JOIN FETCH t.city c " +
           "JOIN FETCH s.movie m " +
           "WHERE s.screen.theater.city.id = :cityId " +
           "AND s.isActive = true " +
           "AND sc.isActive = true " +
           "AND t.isActive = true " +
           "AND c.isActive = true " +
           "AND m.isActive = true")
    List<Show> findByCityId(@Param("cityId") Long cityId);

    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.theater t " +
           "JOIN FETCH t.city c " +
           "JOIN FETCH s.movie m " +
           "WHERE s.movie.id = :movieId " +
           "AND s.screen.theater.city.id = :cityId " +
           "AND s.isActive = true " +
           "AND sc.isActive = true " +
           "AND t.isActive = true " +
           "AND c.isActive = true " +
           "AND m.isActive = true")
    List<Show> findByMovieAndCity(@Param("movieId") Long movieId, @Param("cityId") Long cityId);

    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.theater t " +
           "JOIN FETCH t.city c " +
           "JOIN FETCH s.movie m " +
           "WHERE s.movie.id = :movieId " +
           "AND CAST(s.startTime AS date) = CAST(:date AS date) " +
           "AND s.isActive = true " +
           "AND sc.isActive = true " +
           "AND t.isActive = true " +
           "AND c.isActive = true " +
           "AND m.isActive = true")
    List<Show> findByMovieAndDate(@Param("movieId") Long movieId, @Param("date") LocalDateTime date);

    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.theater t " +
           "JOIN FETCH t.city c " +
           "JOIN FETCH s.movie m " +
           "WHERE s.movie.id = :movieId " +
           "AND s.screen.theater.city.id = :cityId " +
           "AND CAST(s.startTime AS date) = CAST(:date AS date) " +
           "AND s.isActive = true " +
           "AND sc.isActive = true " +
           "AND t.isActive = true " +
           "AND c.isActive = true " +
           "AND m.isActive = true")
    List<Show> findByMovieAndCityAndDate(@Param("movieId") Long movieId, @Param("cityId") Long cityId, @Param("date") LocalDateTime date);

    @Query("SELECT COUNT(s) > 0 FROM Show s WHERE s.screen.id = :screenId " +
           "AND s.isActive = true " +
           "AND (:id IS NULL OR s.id != :id) " +
           "AND (s.startTime < :endTime AND s.endTime > :startTime)")
    boolean existsOverlappingShow(@Param("screenId") Long screenId, 
                                 @Param("startTime") LocalDateTime startTime, 
                                 @Param("endTime") LocalDateTime endTime,
                                 @Param("id") Long id);
}
