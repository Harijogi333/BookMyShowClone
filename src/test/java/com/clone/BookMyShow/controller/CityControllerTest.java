package com.clone.BookMyShow.controller;

import com.clone.BookMyShow.dto.CityRequest;
import com.clone.BookMyShow.dto.CityResponse;
import com.clone.BookMyShow.exception.ResourceNotFoundException;
import com.clone.BookMyShow.service.CityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Slf4j
class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CityService cityService;

    @Autowired
    private ObjectMapper objectMapper;

    private CityRequest cityRequest;
    private CityResponse cityResponse;

    @BeforeEach
    void setUp() {
        cityRequest = new CityRequest();
        cityRequest.setName("Mumbai");
        cityRequest.setIsActive(true);

        cityResponse = CityResponse.builder()
                .id(1L)
                .name("Mumbai")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/cities/add - Success")
    void addCity_Success() throws Exception {
        log.info("Testing POST /api/v1/cities/add success");
        when(cityService.addCity(any(CityRequest.class))).thenReturn(cityResponse);

        mockMvc.perform(post("/api/v1/cities/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cityRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mumbai"));
    }

    @Test
    @DisplayName("PUT /api/v1/cities/{id} - Success")
    void updateCity_Success() throws Exception {
        log.info("Testing PUT /api/v1/cities/1 success");
        when(cityService.updateCity(eq(1L), any(CityRequest.class))).thenReturn(cityResponse);

        mockMvc.perform(put("/api/v1/cities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cityRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mumbai"));
    }

    @Test
    @DisplayName("DELETE /api/v1/cities/{id} - Success")
    void deleteCity_Success() throws Exception {
        log.info("Testing DELETE /api/v1/cities/1 success");

        mockMvc.perform(delete("/api/v1/cities/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/cities/{id} - Success")
    void getCityById_Success() throws Exception {
        log.info("Testing GET /api/v1/cities/1 success");
        when(cityService.getCityById(1L)).thenReturn(cityResponse);

        mockMvc.perform(get("/api/v1/cities/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/cities/{id} - Failure (Not Found)")
    void getCityById_Failure_NotFound() throws Exception {
        log.info("Testing GET /api/v1/cities/1 failure (Not Found)");
        when(cityService.getCityById(1L)).thenThrow(new ResourceNotFoundException("City not found with id: 1"));

        mockMvc.perform(get("/api/v1/cities/1"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/cities - Success")
    void getAllCities_Success() throws Exception {
        log.info("Testing GET /api/v1/cities success");
        when(cityService.getAllCities()).thenReturn(Collections.singletonList(cityResponse));

        mockMvc.perform(get("/api/v1/cities"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Mumbai"));
    }
}
