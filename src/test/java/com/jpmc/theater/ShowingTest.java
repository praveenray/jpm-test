package com.jpmc.theater;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ShowingTest {

    @Test
    void movieIsReqd() {
        Throwable ex = assertThrows(IllegalArgumentException.class, () -> {
            new Showing(null, LocalDateTime.now());
        });
        assertTrue(ex.getMessage().contains("Movie") && ex.getMessage().contains("null"));
    }

    @Test
    void startTimeMustbeWithinBounds() {
        Throwable ex = assertThrows(IllegalArgumentException.class, () -> {
            Movie movie = new Movie("Turning Red", Duration.ofMinutes(85), 20, 1);
            new Showing(movie, LocalDateTime.of(LocalDate.now(), LocalTime.of(0,4)));
        });
        assertTrue(ex.getMessage().contains("Start time must be between"));
    }

    @Test
    void createWithoudSeconds() {
        LocalDateTime dt = LocalDateTime.of(LocalDate.now(), LocalTime.of(11,12,13));
        Showing showing = new Showing(new Movie("Turning Red", Duration.ofMinutes(85), 11, 0),
                                      dt);
        LocalDateTime showDt = showing.getShowStartTime();
        assertNotNull(showDt);

        assertEquals(LocalDateTime.of(dt.getYear(),dt.getMonth(), dt.getDayOfMonth(), dt.getHour(), dt.getMinute(), 0), showDt);
    }

    @Test
    void discountSpecialCode() {
        Movie movie = new Movie("Turning Red", Duration.ofMinutes(85), 20, 1);
        Showing showing = new Showing(movie, LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 12)));
        double price = showing.calculateTicketPrice(List.of(showing));
        assertEquals(16.0, price);
    }

    @Test
    void discountNoSpecialCodeAndFirstShowing() {
        Movie movie = new Movie("Turning Red", Duration.ofMinutes(85), 11, 0);
        Showing showing = new Showing(movie, LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 12)));
        double price = showing.calculateTicketPrice(List.of(showing));
        assertEquals(8.0, price);
    }

    @Test
    void discountSpecialCodeAndFirstShowingOverriding() {
        Movie movie = new Movie("Turning Red", Duration.ofMinutes(85), 11, 1);
        Showing showing = new Showing(movie, LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 12)));
        double price = showing.calculateTicketPrice(List.of(showing));
        assertEquals(8.0, price);
    }

    @Test
    void maxDiscountIsApplied() {
        Movie movie = new Movie("Turning Red", Duration.ofMinutes(10), 20, 1); // 16
        Showing showing1 = new Showing(movie, LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 12)));
        Showing showing2 = new Showing(movie, LocalDateTime.of(LocalDate.of(2022, 1, 7), LocalTime.of(11, 12))); // 4, 2, 5, 1
        Showing showing3 = new Showing(movie, LocalDateTime.of(LocalDate.now(), LocalTime.of(11, 12))); //

        double price = showing2.calculateTicketPrice(List.of(showing1, showing2, showing3));
        assertEquals((movie.getTicketPrice() - 5.0), price);
    }

    // we can use auto generative tests using junit-quickcheck to auto generate a series of samples of Movies and showings
    // and test property of max discounts.
}

