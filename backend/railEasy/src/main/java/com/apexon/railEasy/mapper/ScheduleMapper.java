package com.apexon.railEasy.mapper;

import com.apexon.railEasy.constants.TravelClass;
import com.apexon.railEasy.dto.request.ScheduleRequest;
import com.apexon.railEasy.dto.response.ScheduleResponse;
import com.apexon.railEasy.entity.Schedule;
import com.apexon.railEasy.entity.Train;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps between {@link Schedule} entities and their DTO representations.
 */
@Mapper
public interface ScheduleMapper {

    @Mapping(target = "id", ignore = true)
    Schedule toEntity(ScheduleRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget Schedule schedule, ScheduleRequest request);

    /**
     * @param train         owning train (may be null if not resolved)
     * @param availableSeats available seat count per travel class (may be null)
     */
    @Mapping(target = "id", source = "schedule.id")
    @Mapping(target = "trainNumber", source = "train.trainNumber")
    @Mapping(target = "trainName", source = "train.trainName")
    @Mapping(target = "fares", expression = "java(toFares(schedule))")
    @Mapping(target = "availableSeats", expression = "java(toAvailableSeats(availableSeats))")
    ScheduleResponse toResponse(Schedule schedule, Train train,
                                Map<TravelClass, Integer> availableSeats);

    /** Builds the per-class fare map in a stable order. */
    default Map<String, BigDecimal> toFares(Schedule schedule) {
        Map<String, BigDecimal> fares = new LinkedHashMap<>();
        fares.put(TravelClass.SLEEPER.name(), schedule.getFareSleeper());
        fares.put(TravelClass.AC_3.name(), schedule.getFareAc3());
        fares.put(TravelClass.AC_2.name(), schedule.getFareAc2());
        return fares;
    }

    /** Converts the typed availability map into a name-keyed map (null-safe). */
    default Map<String, Integer> toAvailableSeats(Map<TravelClass, Integer> availableSeats) {
        Map<String, Integer> available = new LinkedHashMap<>();
        if (availableSeats != null) {
            availableSeats.forEach((k, v) -> available.put(k.name(), v));
        }
        return available;
    }
}
