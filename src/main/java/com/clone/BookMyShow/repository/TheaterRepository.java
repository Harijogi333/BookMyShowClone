package com.clone.BookMyShow.repository;

import com.clone.BookMyShow.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Long> {

    @Query("SELECT t FROM Theater t JOIN t.city c WHERE c.id = :cityId AND t.isActive = true AND c.isActive = true")
    List<Theater> findActiveTheatersByCityId(Long cityId);

    @Query("SELECT t FROM Theater t JOIN t.city c WHERE t.isActive = true AND c.isActive = true")
    List<Theater> findAllActiveTheaters();

    List<Theater> findByCityId(Long cityId);
    boolean existsByNameIgnoreCaseAndCityId(String name, Long cityId);
}

