package com.jpmc.theater;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

/*
Prefer Immutable classes
 */
@Value
public class Movie {
    public static final int MIN_TITLE_SIZE = 10;
    public static final int MAX_TITLE_SIZE = 128;
    public static final int MIN_MINUTES = 10;
    public static final int MAX_MINUTES = 5*60;
    public static final double MIN_TICKET_PRICE = 0.0;
    public static final double MAX_TICKET_PRICE = 100.0;
    public static final int MIN_CODE = 0;
    public static final int MAX_CODE = 1;


    private static int MOVIE_CODE_SPECIAL = 1;

    private String title;
    private String description;
    private Duration runningTime;
    private double ticketPrice;
    private int specialCode;

    public Movie(String title, Duration runningTime, double ticketPrice, int specialCode) {
        this(title, title, runningTime, ticketPrice, specialCode);
    }

    public Movie(String title, String description, Duration runningTime, double ticketPrice, int specialCode) {
        if (StringUtils.isBlank(title) || title.trim().length() < MIN_TITLE_SIZE || title.trim().length() > MAX_TITLE_SIZE) {
            throw new IllegalArgumentException(String.format("Movie Title must be between 1 and %s chars", MAX_TITLE_SIZE));
        }
        if (StringUtils.isBlank(description) || description.trim().length() < MIN_TITLE_SIZE || description.trim().length() > MAX_TITLE_SIZE) {
            throw new IllegalArgumentException(String.format("Movie description must be between 1 and %s chars", MAX_TITLE_SIZE));
        }

        long mins = runningTime.toMinutes();
        if (mins < MIN_MINUTES || mins > MAX_MINUTES) {
            throw new IllegalArgumentException(String.format("Duration must be within %s and %s minutes", MIN_MINUTES, MAX_MINUTES));
        }

        if (ticketPrice < MIN_TICKET_PRICE || ticketPrice > MAX_TICKET_PRICE) {
            throw new IllegalArgumentException(String.format("Ticket Price must be within %s and %s", MIN_TICKET_PRICE, MAX_TICKET_PRICE));
        }

        if (specialCode < MIN_CODE || specialCode > MAX_CODE) {
            throw new IllegalArgumentException(String.format("Special code must be within %s and %s", MIN_CODE, MAX_CODE));
        }
        this.title = title;
        this.description = description;
        this.runningTime = runningTime;
        this.ticketPrice = ticketPrice;
        this.specialCode = specialCode;
    }
}