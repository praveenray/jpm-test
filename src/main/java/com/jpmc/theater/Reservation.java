package com.jpmc.theater;

import lombok.Value;

@Value
public class Reservation {
    private Customer customer;
    private Showing showing;
    private int audienceCount;
    private double pricePerPerson;


    public double totalFee() {
        return pricePerPerson * audienceCount;
    }
}