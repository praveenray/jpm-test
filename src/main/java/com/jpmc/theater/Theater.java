package com.jpmc.theater;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Theater {
    private static final int SHOW_CAPACITY = 100;

    LocalDateProvider provider;
    private Map<Movie, List<Showing>> schedule = new HashMap<>();
    private List<Reservation> allReservations = new ArrayList<>();

    public Theater(LocalDateProvider provider) {
        this.provider = provider;
    }

    public boolean addSchedule(Movie movie, LocalDateTime showTime) {
        // schedules are for same date only!
        List<Showing> allShowings = getAllShowings();
        if (allShowings.size() > 0) {
            int existingDay = allShowings.get(0).getShowStartTime().getDayOfYear();
            if (showTime.getDayOfYear() != existingDay) {
                return false;
            }
        }
        List<Showing> movieShowings = new ArrayList<>(schedule.getOrDefault(movie, new ArrayList<>()));
        movieShowings.add(new Showing(movie, showTime));
        movieShowings.sort(Comparator.comparing(Showing::getShowStartTime));
        schedule.put(movie, List.copyOf(movieShowings));
        return true;
    }

    public List<Showing> getMovieShowings(Movie movie) {
        return schedule.getOrDefault(movie, List.of());
    }

    // used for Tests
    public void clearSchedules() {
        schedule.clear();
    }

    public List<Showing> getAllShowings() {
        return schedule.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    public Showing findFirstShowingWithCapacity(List<Showing> showings, int howManyTickets) {
        for(Showing showing : showings) {
            Stream<Reservation> reservations = allReservations.stream().filter(r -> r.getShowing() == showing);
            int customerCount = reservations.map(r -> r.getAudienceCount()).mapToInt(Integer::intValue).sum();
            if ((customerCount + howManyTickets) <= SHOW_CAPACITY) {
                return showing;
            }
        }
        return null;
    }

    public synchronized Reservation reserve(Customer customer, int howManyTickets, Movie movie, LocalDateTime startTime) {
        // do we have a movie showing at that time?
        List<Showing> showings = schedule.getOrDefault(movie, List.of());
        LocalDateTime noSeconds = Showing.removeSeconds(startTime);
        List<Showing> matches = showings.stream().filter(s -> s.getShowStartTime().equals(noSeconds)).collect(Collectors.toList());

        Reservation reservation = null;
        if (!matches.isEmpty()) {
            Showing firstShowingWithEmptySeats = findFirstShowingWithCapacity(matches, howManyTickets);
            if (firstShowingWithEmptySeats != null) {
                double pricePerPerson = firstShowingWithEmptySeats.calculateTicketPrice(matches);
                reservation = new Reservation(customer, firstShowingWithEmptySeats, howManyTickets, pricePerPerson);
                allReservations.add(reservation);
            } else {
                System.err.println("No More empty seats for movie: " + movie + " and " + howManyTickets);
            }
        } else {
            System.err.println("No Showing found for movie " + movie);
        }
        return reservation;
    }

    public List<Reservation> getAllReservations() {
        return List.copyOf(allReservations);
    }

    public void printSchedule() {
        System.out.println(scheduleToString());
    }

    public String scheduleToString() {
        StringBuilder bldr = new StringBuilder(1024);
        bldr.append(provider.currentDate());
        bldr.append("===================================================\n");
        schedule.keySet().stream().forEach(movie -> {
            List<Showing> movieShowings = schedule.getOrDefault(movie, List.of());
            movieShowings.forEach(s ->
                bldr.append(s.computeSequence(movieShowings) + ": "
                                   + s.getShowStartTime() + " "
                                   + s.getMovie().getTitle() + " "
                                   + humanReadableFormat(s.getMovie().getRunningTime())
                                   + " $" + s.calculateTicketPrice(movieShowings))
                        .append("\n")
            );
        });
        bldr.append("===================================================\n");
        return bldr.toString();
    }

    public String scheduleToJson() {
        JsonArrayBuilder arrayBldr = Json.createArrayBuilder();
        schedule.keySet().stream().forEach(movie -> {
            List<Showing> movieShowings = schedule.getOrDefault(movie, List.of());
            movieShowings.stream().forEach (s ->
                arrayBldr.add(
                    Json.createObjectBuilder().add("sequence", s.computeSequence(movieShowings))
                        .add("showTime", s.getShowStartTime().format(DateTimeFormatter.ISO_DATE_TIME))
                        .add("title", s.getMovie().getTitle())
                        .add("runningTime", s.getMovie().getRunningTime().toMinutes())
                        .add("ticketPrice",  s.calculateTicketPrice(movieShowings))
                        .build())
            );
        });

        Map<String, Boolean> configs = new HashMap<>();
        configs.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory factory = Json.createWriterFactory(configs);
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = factory.createWriter(stringWriter);
        writer.writeArray(arrayBldr.build());
        writer.close();
        return stringWriter.toString();
    }

    public String humanReadableFormat(Duration duration) {
        long hour = duration.toHours();
        long remainingMin = duration.toMinutes() - TimeUnit.HOURS.toMinutes(duration.toHours());

        return String.format("(%s hour%s %s minute%s)", hour, handlePlural(hour), remainingMin, handlePlural(remainingMin));
    }

    // (s) postfix should be added to handle plural correctly
    private String handlePlural(long value) {
        if (value == 1) {
            return "";
        }
        else {
            return "s";
        }
    }

    public static void main(String[] args) {
        Theater theater = new Theater(LocalDateProvider.INSTANCE);
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

        theater.printSchedule();
        System.out.println(theater.scheduleToJson());
    }
}
