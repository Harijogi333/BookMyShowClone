package com.clone.BookMyShow.event;

import com.clone.BookMyShow.entity.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookingEventListener {

    @EventListener
    public void handleBookingEvent(BookingEvent event) {
        Booking booking = event.getBooking();
        log.info("Processing Booking Event - ID: {}, Status: {}, User: {}", 
                booking.getId(), booking.getStatus(), booking.getUser().getEmail());

        // Placeholder logic for notifications
        switch (booking.getStatus()) {
            case PENDING -> log.info("Action: Notification sent to user to complete payment for booking {}", booking.getId());
            case CONFIRMED -> log.info("Action: Confirmation email/ticket sent for booking {}", booking.getId());
            case CANCELLED -> log.info("Action: Cancellation confirmation and refund process initiated for booking {}", booking.getId());
            case EXPIRED -> log.info("Action: Seat release notification for expired booking {}", booking.getId());
        }
    }
}
