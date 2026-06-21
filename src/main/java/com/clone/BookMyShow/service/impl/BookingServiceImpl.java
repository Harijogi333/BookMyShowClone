package com.clone.BookMyShow.service.impl;

import com.clone.BookMyShow.dto.BookingRequest;
import com.clone.BookMyShow.dto.BookingResponse;
import com.clone.BookMyShow.entity.*;
import com.clone.BookMyShow.event.BookingEvent;
import com.clone.BookMyShow.exception.ResourceNotFoundException;
import com.clone.BookMyShow.repository.BookingRepository;
import com.clone.BookMyShow.repository.ShowSeatRepository;
import com.clone.BookMyShow.repository.UserRepository;
import com.clone.BookMyShow.security.CustomUserDetails;
import com.clone.BookMyShow.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    @CacheEvict(value = "showSeats", allEntries = true)
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        // 1. Get Authenticated User
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Load ShowSeats with Pessimistic Lock to prevent double booking
        List<ShowSeat> showSeats = showSeatRepository.findByIdInWithLock(bookingRequest.getShowSeatIds());

        if (showSeats.size() != bookingRequest.getShowSeatIds().size()) {
            throw new ResourceNotFoundException("Some selected seats were not found");
        }

        // 3. Validation
        Show show = showSeats.get(0).getShow();
        for (ShowSeat ss : showSeats) {
            if (!ss.getShow().getId().equals(show.getId())) {
                throw new IllegalArgumentException("All seats must belong to the same show");
            }
            if (ss.getStatus() != ShowSeatStatus.AVAILABLE) {
                throw new IllegalStateException("Seat " + ss.getSeat().getSeatNumber() + " is no longer available");
            }
        }

        // 4. Calculate Total Price
        double totalPrice = showSeats.stream().mapToDouble(ShowSeat::getPrice).sum();

        // 5. Create Booking (PENDING status)
        Booking booking = Booking.builder()
                .user(user)
                .show(show)
                .totalPrice(totalPrice)
                .status(BookingStatus.PENDING)
                .bookingTime(LocalDateTime.now())
                .showSeats(showSeats)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // 6. Update Seat Statuses to BLOCKED and link to Booking
        for (ShowSeat ss : showSeats) {
            ss.setStatus(ShowSeatStatus.BLOCKED);
            ss.setBlockedAt(LocalDateTime.now());
            ss.setBooking(savedBooking);
        }
        showSeatRepository.saveAll(showSeats);

        // 7. Publish Event
        applicationEventPublisher.publishEvent(new BookingEvent(savedBooking));

        return mapToResponse(savedBooking);
    }

    @Override
    @Transactional
    @CacheEvict(value = "showSeats", allEntries = true)
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findByIdWithHierarchy(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        validateBookingOwnership(booking);

        if (booking.getShowSeats() == null || booking.getShowSeats().isEmpty()) {
            throw new IllegalStateException("Cannot confirm booking because no seats are assigned.");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be confirmed");
        }

        // 1. Update Booking Status
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);

        // 2. Bulk Update Seats in DB (Efficiency)
        showSeatRepository.updateStatusByBookingId(bookingId, ShowSeatStatus.BOOKED);

        // 3. Update in-memory objects so the response DTO is correct
        for (ShowSeat ss : booking.getShowSeats()) {
            ss.setStatus(ShowSeatStatus.BOOKED);
            ss.setBlockedAt(null);
        }

        // 4. Publish Event
        applicationEventPublisher.publishEvent(new BookingEvent(savedBooking));

        return mapToResponse(savedBooking);
    }

    @Override
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findByIdWithHierarchy(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        validateBookingOwnership(booking);
        return mapToResponse(booking);
    }

    @Override
    public List<BookingResponse> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdWithHierarchy(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "showSeats", allEntries = true)
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findByIdWithHierarchy(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        validateBookingOwnership(booking);

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus()== BookingStatus.EXPIRED) {
            throw new IllegalStateException("Booking is already cancelled or expired");
        }

        // 1. Mark booking as CANCELLED
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // 2. Bulk Release Seats (Efficiency)
        showSeatRepository.releaseSeatsByBookingId(id, ShowSeatStatus.AVAILABLE);

        // 3. Publish Event
        applicationEventPublisher.publishEvent(new BookingEvent(booking));
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getId())
                .userName(booking.getUser().getName())
                .movieTitle(booking.getShow().getMovie().getTitle())
                .theaterName(booking.getShow().getScreen().getTheater().getName())
                .screenName(booking.getShow().getScreen().getName())
                .startTime(booking.getShow().getStartTime())
                .seatNumbers(booking.getShowSeats().stream()
                        .map(ss -> ss.getSeat().getSeatNumber())
                        .collect(Collectors.toList()))
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .bookingTime(booking.getBookingTime())
                .build();
    }

    private void validateBookingOwnership(Booking booking) {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !booking.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to access this booking.");
        }
    }
}
