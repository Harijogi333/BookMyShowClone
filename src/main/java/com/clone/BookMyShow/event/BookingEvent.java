package com.clone.BookMyShow.event;


import com.clone.BookMyShow.entity.Booking;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BookingEvent
{
       private final Booking booking;
       public BookingEvent(Booking booking)
       {
           this.booking = booking;
       }
}

