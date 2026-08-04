-- RailEasy schema (H2 / R2DBC)

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS trains (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_number          VARCHAR(20)  NOT NULL UNIQUE,
    train_name            VARCHAR(120) NOT NULL,
    total_seats_per_class INT          NOT NULL,
    active                BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS schedules (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id       BIGINT        NOT NULL,
    from_station   VARCHAR(120)  NOT NULL,
    to_station     VARCHAR(120)  NOT NULL,
    departure_time TIME          NOT NULL,
    arrival_time   TIME          NOT NULL,
    journey_date   DATE          NOT NULL,
    fare_sleeper   DECIMAL(10,2) NOT NULL,
    fare_ac3       DECIMAL(10,2) NOT NULL,
    fare_ac2       DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_schedule_train FOREIGN KEY (train_id) REFERENCES trains(id),
    CONSTRAINT uq_schedule_slot UNIQUE (train_id, journey_date, departure_time)
);

CREATE TABLE IF NOT EXISTS bookings (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    pnr          VARCHAR(20)   NOT NULL UNIQUE,
    user_id      BIGINT        NOT NULL,
    schedule_id  BIGINT        NOT NULL,
    travel_class VARCHAR(20)   NOT NULL,
    total_fare   DECIMAL(10,2) NOT NULL,
    status       VARCHAR(20)   NOT NULL,
    booked_at    TIMESTAMP     NOT NULL,
    CONSTRAINT fk_booking_user     FOREIGN KEY (user_id)     REFERENCES users(id),
    CONSTRAINT fk_booking_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id)
);

-- Normalized seat inventory: one row per booked seat. The UNIQUE constraint is the
-- authoritative guard against double-booking a seat on a schedule + travel class,
-- and lets the database compute availability with a simple COUNT/GROUP BY.
-- Rows exist only for active bookings; cancelling a booking deletes its seat rows.
CREATE TABLE IF NOT EXISTS booking_seats (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id   BIGINT      NOT NULL,
    schedule_id  BIGINT      NOT NULL,
    travel_class VARCHAR(20) NOT NULL,
    seat_no      VARCHAR(4)  NOT NULL,
    CONSTRAINT fk_booking_seat_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT uq_booking_seat UNIQUE (schedule_id, travel_class, seat_no)
);

-- Secondary indexes for the hot read paths.
-- Booking lookups by schedule (deletion guard) and user's ticket list.
CREATE INDEX IF NOT EXISTS idx_bookings_schedule
    ON bookings (schedule_id);
-- "My Tickets": list a user's bookings newest-first.
CREATE INDEX IF NOT EXISTS idx_bookings_user_booked_at
    ON bookings (user_id, booked_at);
-- Seat-availability lookups (hottest): occupancy by schedule + travel class.
CREATE INDEX IF NOT EXISTS idx_booking_seats_schedule_class
    ON booking_seats (schedule_id, travel_class);
-- Seat retrieval for a given booking.
CREATE INDEX IF NOT EXISTS idx_booking_seats_booking
    ON booking_seats (booking_id);
-- Train search filters primarily by journey date.
CREATE INDEX IF NOT EXISTS idx_schedules_journey_date
    ON schedules (journey_date);
-- Schedules-by-train lookups and FK joins.
CREATE INDEX IF NOT EXISTS idx_schedules_train_id
    ON schedules (train_id);
