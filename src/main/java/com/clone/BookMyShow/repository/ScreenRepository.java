package com.clone.BookMyShow.repository;

import com.clone.BookMyShow.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {

    @Query("SELECT s FROM Screen s " +
           "JOIN FETCH s.theater t " +
           "JOIN FETCH t.city c " +
           "JOIN FETCH t.owner o " +
           "WHERE s.id = :id")
    java.util.Optional<Screen> findByIdWithHierarchy(@Param("id") Long id);

    @Query("SELECT s FROM Screen s JOIN FETCH s.theater t JOIN FETCH t.city c WHERE t.id = :theaterId AND t.isActive = true AND c.isActive = true")
    List<Screen> findScreensByTheaterId(@Param("theaterId") Long theaterId);

    List<Screen> findByTheaterId(Long theaterId);
    boolean existsByNameIgnoreCaseAndTheaterId(String name, Long theaterId);
}
