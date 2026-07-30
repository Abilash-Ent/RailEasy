package com.apexon.railEasy.constants;

/**
 * Centralized application-wide constant values.
 */
public final class AppConstants {

    private AppConstants() {
        throw new IllegalStateException("Constant class cannot be instantiated");
    }

    // API base paths
    public static final String API_BASE = "/api/v1";
    public static final String AUTH_BASE = API_BASE + "/auth";
    public static final String TRAIN_BASE = API_BASE + "/trains";
    public static final String SCHEDULE_BASE = API_BASE + "/schedules";
    public static final String BOOKING_BASE = API_BASE + "/bookings";

    // Security
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_USER_ID = "userId";

    // Booking / seat layout (8 rows x 8 seats = 64 seats per class)
    public static final int SEAT_ROWS = 8;
    public static final int SEAT_COLUMNS = 8;
    public static final int SEATS_PER_CLASS = SEAT_ROWS * SEAT_COLUMNS;
    public static final int MIN_SEATS_PER_BOOKING = 1;
    public static final int MAX_SEATS_PER_BOOKING = 4;
    public static final String SEAT_PATTERN = "^[1-8][A-H]$";
}

