package com.apexon.railEasy.util;

import com.apexon.railEasy.constants.AppConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Helpers for the fixed 8x8 (64) seat layout used for every travel class.
 * Seat labels are {@code <row><column>}, e.g. {@code 1A} .. {@code 8H}.
 */
public final class SeatUtils {

    private static final Pattern SEAT = Pattern.compile(AppConstants.SEAT_PATTERN);

    private SeatUtils() {
    }

    /** @return all 64 seat labels for a class, ordered 1A..1H, 2A..8H. */
    public static List<String> allSeats() {
        List<String> seats = new ArrayList<>(AppConstants.SEATS_PER_CLASS);
        for (int row = 1; row <= AppConstants.SEAT_ROWS; row++) {
            for (int col = 0; col < AppConstants.SEAT_COLUMNS; col++) {
                seats.add(row + String.valueOf((char) ('A' + col)));
            }
        }
        return seats;
    }

    public static boolean isValid(String seat) {
        return seat != null && SEAT.matcher(seat).matches();
    }
}

