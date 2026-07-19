package com.clone.BookMyShow.repository;

import com.clone.BookMyShow.dto.Suggestion;
import com.clone.BookMyShow.entity.Movie;
import com.clone.BookMyShow.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByTitleAndLanguage(String title, Language language);
    List<Movie> findByIsActiveTrue();

    @Query("SELECT new com.clone.BookMyShow.dto.Suggestion(m.id,m.title,'movie') FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :movieName, '%'))")
    List<Suggestion> getMatchedMovies(@Param("movieName") String movieName);
}
