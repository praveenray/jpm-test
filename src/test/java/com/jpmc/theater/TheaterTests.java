package com.jpmc.theater;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.json.*;
import java.io.StringReader;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TheaterTests {
    private Theater theater = null;

    @BeforeEach
    public void beforeEach() {
        theater = new Theater(LocalDateProvider.INSTANCE);
        theater.clearSchedules();
        generateSampleSchedules();
    }

    @Test
    void cannotAddDifferentDate() {
        List<Showing> showings = theater.getAllShowings();
        Showing first = showings.get(0);

        assertFalse(theater.addSchedule(first.getMovie(), first.getShowStartTime().minusDays(1)));
        assertEquals(showings.size(), theater.getAllShowings().size());
    }

    @Test
    void noReservationsUntilSchedule() {
        theater.clearSchedules();
        assertNull(theater.reserve(
                new Customer("x", "1"),
                1,
                new Movie("Turning Red", Duration.ofMinutes(85), 11, 0),
                LocalDateTime.now()
        ));
    } 
    
    @Test
    void noReservationsForWrongMovie() {
        assertNull(theater.reserve(
                new Customer("x", "1"),
                1,
                new Movie("Turning Red not found", Duration.ofMinutes(85), 11, 0),
                LocalDateTime.now()
        ));
    }
    
    @Test
    void noReservationsForWrongDate() {
        List<Showing> showings = theater.getAllShowings();
        Showing showing = showings.get(0);
        assertNull(theater.reserve(
                new Customer("x", "1"),
                1,
                showing.getMovie(),
                showing.getShowStartTime().minusDays(1)
        ));
    }

    @Test
    void noReservationsForWrongHours() {
        List<Showing> showings = theater.getAllShowings();
        Showing showing = showings.get(0);
        assertNull(theater.reserve(
                new Customer("x", "1"),
                1,
                showing.getMovie(),
                showing.getShowStartTime().minusHours(1)
        ));
    }
    
    @Test
    void makeValidReservation() {
        List<Reservation> existing = theater.getAllReservations();
        assertEquals(0, existing.size());

        List<Showing> showings = theater.getAllShowings();
        Showing showing = showings.get(0);
        Reservation reservation = theater.reserve(
                new Customer("x", "1"),
                1,
                showing.getMovie(),
                showing.getShowStartTime()
        );
        assertNotNull(reservation);
        assertEquals(1, theater.getAllReservations().size());
    } 

    @Test
    void firstMovieDiscount() {
        Movie movie = theater.getAllShowings().get(0).getMovie();
        List<Showing> showings = theater.getMovieShowings(movie);
        assertEquals(8.0D, showings.get(0).calculateTicketPrice(showings));
    }

    @Test
    void secondMovieDiscount() {
        Movie movie = theater.getAllShowings().get(0).getMovie();
        List<Showing> showings = theater.getMovieShowings(movie);
        assertEquals(8.25D, showings.get(1).calculateTicketPrice(showings));
    }


    @Test
    void schedulesStringIsGenerated() {
        String str = theater.scheduleToString();
        assertNotNull(str);
    }

    @Test
    void scheduledJsonIsGenerated() {
        String str = theater.scheduleToJson();
        assertFalse(StringUtils.isBlank(str));

        JsonReader reader = Json.createReader(new StringReader(str));
        JsonArray array = reader.readArray();
        List<Showing> allShowings = theater.getAllShowings();
        assertEquals(array.size(), allShowings.size());
        assertFalse(array.isEmpty());
        JsonObject showing1 = array.get(0).asJsonObject();
        assertNotNull(showing1);
        assertEquals(allShowings.get(0).computeSequence(allShowings), showing1.getInt("sequence"));
        assertEquals(allShowings.get(0).getShowStartTime().format(DateTimeFormatter.ISO_DATE_TIME), showing1.getString("showTime"));
        assertEquals(allShowings.get(0).getMovie().getTitle(), showing1.getString("title"));
        assertEquals(allShowings.get(0).getMovie().getRunningTime().toMinutes(), showing1.getInt("runningTime"));
        assertEquals(allShowings.get(0).calculateTicketPrice(allShowings) , showing1.getInt("ticketPrice"));
    }

    // used for Tests
    private void generateSampleSchedules() {
        Movie turningRed = new Movie("Turning Red", Duration.ofMinutes(85), 11, 0);
        Movie theBatMan = new Movie("The Batman", Duration.ofMinutes(95), 9, 0);
        Movie spiderMan = new Movie("Spider-Man: No Way Home", Duration.ofMinutes(90), 12.5, 1);
        LocalDate currentDate = LocalDateProvider.INSTANCE.currentDate();


        theater.addSchedule(turningRed, LocalDateTime.of(currentDate, LocalTime.of(9, 0)));
        theater.addSchedule(spiderMan, LocalDateTime.of(currentDate, LocalTime.of(11, 0)));
        theater.addSchedule(theBatMan, LocalDateTime.of(currentDate, LocalTime.of(12, 50)));
        theater.addSchedule(turningRed, LocalDateTime.of(currentDate, LocalTime.of(14, 30)));
        theater.addSchedule(spiderMan, LocalDateTime.of(currentDate, LocalTime.of(16, 10)));
        theater.addSchedule(theBatMan, LocalDateTime.of(currentDate, LocalTime.of(17, 50)));
        theater.addSchedule(turningRed, LocalDateTime.of(currentDate, LocalTime.of(19, 30)));
        theater.addSchedule(spiderMan, LocalDateTime.of(currentDate, LocalTime.of(21, 10)));
        theater.addSchedule(theBatMan, LocalDateTime.of(currentDate, LocalTime.of(23, 0)));
    }
}
