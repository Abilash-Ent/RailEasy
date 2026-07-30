package com.apexon.railEasy.mapper;

import com.apexon.railEasy.dto.response.BookingResponse;
import com.apexon.railEasy.entity.Booking;
import com.apexon.railEasy.entity.Schedule;
import com.apexon.railEasy.entity.Train;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.List;

/**
 * Maps between {@link Booking} entities and their DTO representations.
 */
@Mapper
public interface BookingMapper {

    @Mapping(target = "id", source = "booking.id")
    @Mapping(target = "trainNumber", source = "train.trainNumber")
    @Mapping(target = "trainName", source = "train.trainName")
    @Mapping(target = "fromStation", source = "schedule.fromStation")
    @Mapping(target = "toStation", source = "schedule.toStation")
    @Mapping(target = "journeyDate", source = "schedule.journeyDate")
    @Mapping(target = "seatNumbers", expression = "java(BookingMapper.parseSeats(booking.getSeatNumbers()))")
    BookingResponse toResponse(Booking booking, Schedule schedule, Train train);

    /** Parses the comma-separated seat labels stored on a booking (e.g. "1A,1B"). */
    static List<String> parseSeats(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}



