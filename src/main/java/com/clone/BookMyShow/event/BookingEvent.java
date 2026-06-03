package com.clone.BookMyShow.event;


import com.clone.BookMyShow.entity.Booking;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BookingEvent extends ApplicationEvent
{
       private final Booking booking;
       public BookingEvent(Object source, Booking booking)
       {
             super(source);
             this.booking = booking;
       }
}

