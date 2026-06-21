package com.clone.BookMyShow.service.impl;

import com.clone.BookMyShow.dto.ShowRequest;
import com.clone.BookMyShow.dto.ShowResponse;
import com.clone.BookMyShow.dto.ShowSeatResponse;
import com.clone.BookMyShow.entity.*;
import com.clone.BookMyShow.exception.ResourceAlreadyExistsException;
import com.clone.BookMyShow.exception.ResourceNotFoundException;
import com.clone.BookMyShow.repository.*;
import com.clone.BookMyShow.security.CustomUserDetails;
import com.clone.BookMyShow.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowServiceImpl implements ShowService {

    private static final int CLEANING_BUFFER_MINUTES = 20;

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowSeatRepository showSeatRepository;

    @Override
    @Transactional
    @CacheEvict(value = "shows", allEntries = true)
    public ShowResponse addShow(ShowRequest showRequest) {
        // Validation: Start time cannot be in the past
        if (showRequest.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule a show in the past.");
        }

        Movie movie = movieRepository.findById(showRequest.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + showRequest.getMovieId()));
        
        if (!movie.getIsActive()) {
            throw new IllegalArgumentException("Cannot schedule show for an inactive movie.");
        }

        // Optimized: Fetch Screen, Theater, City, and Owner in ONE query to avoid N+1
        Screen screen = screenRepository.findByIdWithHierarchy(showRequest.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with id: " + showRequest.getScreenId()));

        Theater theater = screen.getTheater();
        validateTheaterOwnership(theater);

        validateHierarchy(theater, screen);

        LocalDateTime endTime = showRequest.getStartTime().plusMinutes(movie.getDurationInMinutes() + CLEANING_BUFFER_MINUTES);

        validateNoTimeOverlap(screen.getId(), showRequest.getStartTime(), endTime, null);

        Show show = new Show();
        show.setMovie(movie);
        show.setScreen(screen);
        show.setStartTime(showRequest.getStartTime());
        show.setEndTime(endTime);
        show.setIsActive(showRequest.getIsActive() != null ? showRequest.getIsActive() : true);

        Show savedShow = showRepository.save(show);

        createShowSeats(savedShow, screen, showRequest.getSeatTypePrices());

        return mapToResponse(savedShow);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "shows", allEntries = true),
        @CacheEvict(value = "showSeats", key = "#id")
    })
    public ShowResponse updateShow(Long id, ShowRequest showRequest) {
        Show show = showRepository.findByIdWithHierarchy(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + id));

        validateTheaterOwnership(show.getScreen().getTheater());

        // Validation: Start time cannot be in the past
        if (showRequest.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot update a show to a past time.");
        }

        Movie movie = movieRepository.findById(showRequest.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + showRequest.getMovieId()));
        
        LocalDateTime endTime = showRequest.getStartTime().plusMinutes(movie.getDurationInMinutes() + CLEANING_BUFFER_MINUTES);

        boolean isScreenChanged = !show.getScreen().getId().equals(showRequest.getScreenId());
        boolean isMovieChanged = !show.getMovie().getId().equals(showRequest.getMovieId());
        boolean isTimeChanged = !show.getStartTime().equals(showRequest.getStartTime());
        
        // If movie, screen, or time is changing, check if any seats are already BOOKED or BLOCKED
        if (isMovieChanged || isScreenChanged || isTimeChanged) {
            validateNoExistingBookings(id);
        }

        if (isScreenChanged) {
            // Optimized: Fetch hierarchy for new screen
            Screen newScreen = screenRepository.findByIdWithHierarchy(showRequest.getScreenId())
                    .orElseThrow(() -> new ResourceNotFoundException("New screen not found with id: " + showRequest.getScreenId()));
            
            validateHierarchy(newScreen.getTheater(), newScreen);
            validateNoTimeOverlap(newScreen.getId(), showRequest.getStartTime(), endTime, id);
            
            // Efficient bulk delete of old show seats
            showSeatRepository.deleteByShowId(id);
            
            show.setScreen(newScreen);
            createShowSeats(show, newScreen, showRequest.getSeatTypePrices());
        } else {
            validateNoTimeOverlap(show.getScreen().getId(), showRequest.getStartTime(), endTime, id);
            
            // Update prices if they've changed
            List<ShowSeat> currentSeats = showSeatRepository.findByShowId(id);
            // Check if any price needs updating    
            boolean needsPriceUpdate = currentSeats.stream().anyMatch(ss -> {
                Double newPrice = showRequest.getSeatTypePrices().get(ss.getSeat().getSeatType());
                return newPrice != null && !newPrice.equals(ss.getPrice());
            });

            if (needsPriceUpdate) {
                validateNoExistingBookings(id);
                currentSeats.forEach(ss -> {
                    Double newPrice = showRequest.getSeatTypePrices().get(ss.getSeat().getSeatType());
                    if (newPrice != null) {
                        ss.setPrice(newPrice);
                    }
                });
                showSeatRepository.saveAll(currentSeats);
            }
        }

        show.setMovie(movie);
        show.setStartTime(showRequest.getStartTime());
        show.setEndTime(endTime);
        if (showRequest.getIsActive() != null) {
            show.setIsActive(showRequest.getIsActive());
        }

        Show updatedShow = showRepository.save(show);
        return mapToResponse(updatedShow);
    }

    private void createShowSeats(Show show, Screen screen, Map<SeatType, Double> prices) {
        List<Seat> physicalSeats = seatRepository.findByScreenId(screen.getId());
        List<ShowSeat> showSeats = physicalSeats.stream().map(seat -> {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setShow(show);
            showSeat.setSeat(seat);
            showSeat.setStatus(ShowSeatStatus.AVAILABLE);
            Double price = prices.getOrDefault(seat.getSeatType(), 0.0);
            showSeat.setPrice(price);
            return showSeat;
        }).collect(Collectors.toList());

        showSeatRepository.saveAll(showSeats);
    }

    private void validateNoExistingBookings(Long showId) {
        // Optimized: Single lightweight database check instead of loading hundreds of objects
        boolean hasBookings = showSeatRepository.existsByShowIdAndStatusIn(
                showId, List.of(ShowSeatStatus.BOOKED, ShowSeatStatus.BLOCKED));
        
        if (hasBookings) {
            throw new IllegalStateException("Cannot change screen or time for a show that already has booked or blocked seats.");
        }
    }

    private void validateNoTimeOverlap(Long screenId, LocalDateTime start, LocalDateTime end, Long showId) {
        if (showRepository.existsOverlappingShow(screenId, start, end, showId)) {
            throw new ResourceAlreadyExistsException(
                "Time conflict! This screen is already occupied by another show during the requested interval (including 20-min cleaning buffer).");
        }
    }

    private void validateHierarchy(Theater theater, Screen screen) {
        if (!theater.getCity().getIsActive()) {
            throw new ResourceNotFoundException("Cannot schedule show because city '" + theater.getCity().getName() + "' is inactive.");
        }
        if (!theater.getIsActive()) {
            throw new ResourceNotFoundException("Cannot schedule show because theater '" + theater.getName() + "' is inactive.");
        }
        if (!screen.getIsActive()) {
            throw new ResourceNotFoundException("Cannot schedule show because screen '" + screen.getName() + "' is inactive.");
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "shows", allEntries = true),
        @CacheEvict(value = "showSeats", key = "#id")
    })
    public void deleteShow(Long id) {
        Show show = showRepository.findByIdWithHierarchy(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + id));
        validateTheaterOwnership(show.getScreen().getTheater());
        
        // Optimized: Check for bookings without loading entities
        boolean hasBookings = showSeatRepository.existsByShowIdAndStatusIn(id, List.of(ShowSeatStatus.BOOKED));

        if (!hasBookings) {
            // Efficient bulk delete
            showSeatRepository.deleteByShowId(id);
        } else {
            // Optimized: Efficient bulk update to CANCELLED instead of looping in Java
            showSeatRepository.updateStatusByShowId(id, ShowSeatStatus.CANCELLED, 
                    List.of(ShowSeatStatus.BOOKED, ShowSeatStatus.BLOCKED));
        }
        
        show.setIsActive(false);
        showRepository.save(show);
    }

    private void validateTheaterOwnership(Theater theater) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTheaterOwner = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_THEATER_OWNER"));
        
        if (isAdmin) return;
        
        if (isTheaterOwner && theater.getOwner().getId().equals(currentUser.getId())) {
            return;
        }
        throw new AccessDeniedException("You do not have permission to manage shows for this theater.");
    }

    @Override
    @Cacheable(value = "shows", key = "#id")
    public ShowResponse getShowById(Long id) {
        Show show = showRepository.findByIdWithHierarchy(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + id));
        return mapToResponse(show);
    }

    @Override
    public List<ShowResponse> getShowsByScreen(Long screenId) {
        return showRepository.findByScreenId(screenId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShowResponse> getShowsByTheater(Long theaterId) {
        return showRepository.findByTheaterId(theaterId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShowResponse> getShowsByMovie(Long movieId) {
        return showRepository.findByMovieId(movieId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "shows", key = "'city:' + #cityId")
    public List<ShowResponse> getShowsByCity(Long cityId) {
        return showRepository.findByCityId(cityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "shows", key = "'movie:' + #movieId + ':city:' + #cityId")
    public List<ShowResponse> getShowsByMovieAndCity(Long movieId, Long cityId) {
        return showRepository.findByMovieAndCity(movieId, cityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShowResponse> getShowsByMovieAndDate(Long movieId, java.time.LocalDate date) {
        return showRepository.findByMovieAndDate(movieId, date.atStartOfDay()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "shows", key = "'active'")
    public List<ShowResponse> getActiveShows() {
        return showRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "showSeats", key = "#showId")
    public List<ShowSeatResponse> getShowSeats(Long showId) {
        if (!showRepository.existsById(showId)) {
            throw new ResourceNotFoundException("Show not found with id: " + showId);
        }
        return showSeatRepository.findByShowIdWithSeat(showId).stream()
                .map(this::mapToShowSeatResponse)
                .collect(Collectors.toList());
    }

    private ShowSeatResponse mapToShowSeatResponse(ShowSeat showSeat) {
        return ShowSeatResponse.builder()
                .id(showSeat.getId())
                .seatId(showSeat.getSeat().getId())
                .seatNumber(showSeat.getSeat().getSeatNumber())
                .seatType(showSeat.getSeat().getSeatType())
                .price(showSeat.getPrice())
                .status(showSeat.getStatus())
                .build();
    }

    private ShowResponse mapToResponse(Show show) {
        return ShowResponse.builder()
                .id(show.getId())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .movieId(show.getMovie().getId())
                .movieTitle(show.getMovie().getTitle())
                .screenId(show.getScreen().getId())
                .screenName(show.getScreen().getName())
                .theaterName(show.getScreen().getTheater().getName())
                .cityName(show.getScreen().getTheater().getCity().getName())
                .isActive(show.getIsActive())
                .build();
    }
}
