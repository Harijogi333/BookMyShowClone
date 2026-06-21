package com.clone.BookMyShow.task;

import com.clone.BookMyShow.entity.Booking;
import com.clone.BookMyShow.entity.BookingStatus;
import com.clone.BookMyShow.entity.ShowSeat;
import com.clone.BookMyShow.entity.ShowSeatStatus;
import com.clone.BookMyShow.repository.BookingRepository;
import com.clone.BookMyShow.repository.ShowSeatRepository;
import com.clone.BookMyShow.event.BookingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingCleanupTask {

    private final BookingRepository bookingRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ApplicationEventPublisher eventPublisher;

    // TTL for seat blocks (e.g., 5 minutes)
    private static final int TTL_MINUTES = 2;

    /**
     * Scheduled task to clean up expired PENDING bookings and their associated BLOCKED seats.
     * Runs every minute.
     */
    @Scheduled(fixedRate = 60000) // 1 minute
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(TTL_MINUTES);
        List<Booking> expiredBookings = bookingRepository.findByStatusAndBookingTimeBefore(BookingStatus.PENDING, expiryTime);

        if (!expiredBookings.isEmpty()) {
            log.info("Found {} expired bookings. Cleaning up...", expiredBookings.size());
            
            for (Booking booking : expiredBookings) {
                // 1. Mark booking as EXPIRED
                booking.setStatus(BookingStatus.EXPIRED);
                
                // 2. Release associated seats
                List<ShowSeat> showSeats = booking.getShowSeats();
                for (ShowSeat ss : showSeats) {
                    ss.setStatus(ShowSeatStatus.AVAILABLE);
                    ss.setBlockedAt(null);
                    ss.setBooking(null);
                }
                showSeatRepository.saveAll(showSeats);

                // 3. Publish Event
                eventPublisher.publishEvent(new BookingEvent(booking));
            }
            bookingRepository.saveAll(expiredBookings);
            log.info("Cleanup completed.");
        }
    }
}
