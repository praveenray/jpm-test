package com.jpmc.theater;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MovieTests {
    @Test
    public void createGoodMovies() {
        int trials = 0;
        Random r = new Random();
        while (trials++ < 1000) {
            int minutes = r.nextInt(Movie.MAX_MINUTES - Movie.MIN_MINUTES + 1) + Movie.MIN_MINUTES;
            double price = r.nextInt(Double.valueOf(Movie.MAX_TICKET_PRICE).intValue() - Double.valueOf(Movie.MIN_TICKET_PRICE + 1.0).intValue())
                                    + Movie.MIN_TICKET_PRICE;
            int code = r.nextInt(Movie.MAX_CODE + 1);
            Movie movie = new Movie(UUID.randomUUID().toString(), Duration.ofMinutes(minutes), price, code);
            assertNotNull(movie);
            validateMovie(movie);
        }
    }

    private void validateMovie(Movie movie) {
        assertTrue(movie.getTitle().length() <= Movie.MAX_TITLE_SIZE);
        assertTrue(movie.getTitle().length() >= Movie.MIN_TITLE_SIZE);

        assertTrue(movie.getTicketPrice() >= Movie.MIN_TICKET_PRICE && movie.getTicketPrice() <= Movie.MAX_TICKET_PRICE);
        assertTrue(movie.getSpecialCode() >= Movie.MIN_CODE && movie.getSpecialCode() <= Movie.MAX_CODE);
        assertTrue(movie.getRunningTime().toMinutes() >= Movie.MIN_MINUTES && movie.getRunningTime().toMinutes() <= Movie.MAX_MINUTES);
    }

    @Test
    public void createInvalidMovies() {
        Random r = new Random();
        int trials = 0;
        while(trials++ < 1000) {
            int minutes = r.nextInt(Movie.MAX_MINUTES + 100 - Movie.MIN_MINUTES + 1) + Movie.MIN_MINUTES;
            double price = r.nextInt(Double.valueOf(Movie.MAX_TICKET_PRICE + 10.0).intValue() - Double.valueOf(Movie.MIN_TICKET_PRICE + 1.0).intValue())
                    + Movie.MIN_TICKET_PRICE;
            int code = r.nextInt(Movie.MAX_CODE + 1);

            try {
                Movie movie = new Movie(UUID.randomUUID().toString(), Duration.ofMinutes(minutes), price, code);
                assertNotNull(movie);
                validateMovie(movie);
            } catch (IllegalArgumentException e) {
                if (minutes < Movie.MIN_MINUTES || minutes > Movie.MAX_MINUTES) {
                    assertTrue(e.getMessage().contains("Duration"));
                } else if (price < Movie.MIN_TICKET_PRICE || price > Movie.MAX_TICKET_PRICE) {
                    assertEquals(String.format("Ticket Price must be within %s and %s", Movie.MIN_TICKET_PRICE, Movie.MAX_TICKET_PRICE),
                            e.getMessage()
                    );
                } else if (code < Movie.MIN_CODE || code > Movie.MAX_CODE) {
                    assertTrue(e.getMessage().contains("Special Code"));
                }
            }
        }
    }
}
