package com.clone.BookMyShow.repository;

import com.clone.BookMyShow.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {

    @Query("SELECT s FROM Screen s JOIN s.theater t JOIN t.city c WHERE t.id = :theaterId AND s.isActive = true AND t.isActive = true AND c.isActive = true")
    List<Screen> findActiveScreensByTheaterId(Long theaterId);

    List<Screen> findByTheaterId(Long theaterId);
    boolean existsByNameIgnoreCaseAndTheaterId(String name, Long theaterId);
}
