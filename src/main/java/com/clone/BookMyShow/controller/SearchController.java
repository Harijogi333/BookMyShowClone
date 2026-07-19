package com.clone.BookMyShow.controller;

import com.clone.BookMyShow.dto.Suggestion;
import com.clone.BookMyShow.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/{word}")
    public ResponseEntity<List<Suggestion>> showMatchedString(@PathVariable String word,
                                                              @RequestParam(defaultValue = "10") int limit,
                                                              @RequestParam(required = false) String type)
    {
        if(word==null || word.isEmpty() || word.isBlank())
        {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(searchService.search(word, limit, type));
    }
}
