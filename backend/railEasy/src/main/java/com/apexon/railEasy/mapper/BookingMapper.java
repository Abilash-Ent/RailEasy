package com.apexon.railEasy.mapper;

import com.apexon.railEasy.dto.response.BookingResponse;
import com.apexon.railEasy.entity.Booking;
import com.apexon.railEasy.entity.Schedule;
import com.apexon.railEasy.entity.Train;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
    @Mapping(target = "seatNumbers", source = "seats")
    BookingResponse toResponse(Booking booking, Schedule schedule, Train train, List<String> seats);

}
