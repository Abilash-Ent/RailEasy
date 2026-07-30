package com.apexon.railEasy.service;

import com.apexon.railEasy.constants.TravelClass;
import com.apexon.railEasy.dto.request.ScheduleRequest;
import com.apexon.railEasy.dto.response.ScheduleResponse;
import com.apexon.railEasy.dto.response.SeatAvailabilityResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Schedule management, search and seat-availability operations.
 */
public interface ScheduleService {

    Mono<ScheduleResponse> create(ScheduleRequest request);

    Mono<ScheduleResponse> update(Long id, ScheduleRequest request);

    Mono<Void> delete(Long id);

    Mono<ScheduleResponse> getById(Long id);

    Flux<ScheduleResponse> getAll();

    Flux<ScheduleResponse> getByTrain(Long trainId);

    Flux<ScheduleResponse> search(String from, String to, LocalDate date);

    Mono<SeatAvailabilityResponse> getSeatAvailability(Long scheduleId, TravelClass travelClass);
}

