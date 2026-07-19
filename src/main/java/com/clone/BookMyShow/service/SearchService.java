package com.clone.BookMyShow.service;

import com.clone.BookMyShow.dto.Suggestion;

import java.util.List;

public interface SearchService {

    List<Suggestion> search(String word, int limit, String type);
}
