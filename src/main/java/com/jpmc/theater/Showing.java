package com.jpmc.theater;

import lombok.Value;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.OptionalDouble;

@Value
public class Showing {
    public static final LocalTime MIN_START_TIME = LocalTime.of(9, 0);
    public static final LocalTime MAX_START_TIME = LocalTime.of(23, 0);
    private static final Double NO_DISCOUNT = 0.0;

    private Movie movie;
    private LocalDateTime showStartTime;

    public Showing(Movie movie, LocalDateTime showStartTime) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie can not be null");
        }

        LocalTime time = showStartTime.toLocalTime();
        if (time.isBefore(MIN_START_TIME) || time.isAfter(MAX_START_TIME)) {
            throw new IllegalArgumentException(String.format("Start time must be between %s and %s", MIN_START_TIME, MAX_START_TIME));
        }
        this.movie = movie;
        this.showStartTime = removeSeconds(showStartTime);
    }

    public int computeSequence(List<Showing> allShowingsForMovie) {
        int seq = 0;
        // assumption: allShowingsForMovie is sorted by showStartTime
        for (Showing showing: allShowingsForMovie) {
            if (showing.getMovie() == movie && this == showing) {
                return ++seq;
            }
            seq++;
        }
        return 0; // not found!
    }

    public static LocalDateTime removeSeconds(LocalDateTime myTime) {
        return LocalDateTime.of(
                myTime.getYear(),
                myTime.getMonth(),
                myTime.getDayOfMonth(),
                myTime.getHour(),
                myTime.getMinute(),
                0
        );
    }

    /*
        Since ticket price is dependent upon showing timings, this is the best place to
        perform ticket price computations
     */
    public double calculateTicketPrice(List<Showing> allShowingsForMovie) {
        Double discount = NO_DISCOUNT;
        for(Showing showing: allShowingsForMovie) {
            if (showing.getMovie() == movie) {
                OptionalDouble maxDiscount = List.of(
                    discountSpecialCode(),
                    discountFirstShowing(allShowingsForMovie),
                    discountSecondShowing(allShowingsForMovie),
                    discountAfternoons(),
                    discountSeventhOfMonth()
                ).stream().mapToDouble(v -> v).max();
                if (maxDiscount.isPresent()) {
                    discount = maxDiscount.getAsDouble();
                }
            }
        }

        double finalPrice = movie.getTicketPrice() - discount;
        if (finalPrice < 0.0) {
            finalPrice = 0.0;
        }
        return finalPrice;
    }


    private Double discountSpecialCode() {
        int specialCode = movie.getSpecialCode();
        if (specialCode != 0) {
            return 0.2 * movie.getTicketPrice();
        }
        return NO_DISCOUNT;
    }

    private double discountFirstShowing(List<Showing> allShowingsForMovie) {
        if (!allShowingsForMovie.isEmpty()) {
            if (allShowingsForMovie.get(0) == this) {
                return 3.0;
            }
        }
        return NO_DISCOUNT;
    }

    private double discountSecondShowing(List<Showing> allShowingsForMovie) {
        if (allShowingsForMovie.size() >= 2) {
            if (allShowingsForMovie.get(1) == this) {
                return 2.0;
            }
        }
        return NO_DISCOUNT;
    }

    private double discountAfternoons() {
        int hr = showStartTime.getHour();
        if (hr >= 11 && hr <= 16) {
            return movie.getTicketPrice() * 0.25;
        }
        return NO_DISCOUNT;
    }

    private double discountSeventhOfMonth() {
        if (showStartTime.getDayOfMonth() == 7) {
            return 1.0;
        }
        return NO_DISCOUNT;
    }
}
