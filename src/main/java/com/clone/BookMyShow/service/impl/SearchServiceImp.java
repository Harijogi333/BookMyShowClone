package com.clone.BookMyShow.service.impl;

import com.clone.BookMyShow.dto.Suggestion;
import com.clone.BookMyShow.repository.MovieRepository;
import com.clone.BookMyShow.repository.TheaterRepository;
import com.clone.BookMyShow.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImp implements SearchService {

    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;

    @Override
    public List<Suggestion> search(String word, int limit, String type) {

        List<Suggestion> matchedStrings=new ArrayList<>();

        if (type == null || type.equalsIgnoreCase("movie")) {
            List<Suggestion> moviesList=movieRepository.getMatchedMovies(word);
            matchedStrings.addAll(moviesList);
        }

        if (type == null || type.equalsIgnoreCase("theatre")) {
            List<Suggestion> theatresList=theaterRepository.getMatchedTheaters(word);
            matchedStrings.addAll(theatresList);
        }

        return matchedStrings.size() > limit ? matchedStrings.subList(0, limit) : matchedStrings;

    }
}
