package com.clone.BookMyShow.service;

import com.clone.BookMyShow.dto.CityRequest;
import com.clone.BookMyShow.dto.CityResponse;

import java.util.List;

public interface CityService {
    CityResponse addCity(CityRequest cityRequest);
    CityResponse updateCity(Long id, CityRequest cityRequest);
    void deleteCity(Long id); // Soft delete
    CityResponse getCityById(Long id);
    List<CityResponse> getAllCities();
    List<CityResponse> getActiveCities();
}
