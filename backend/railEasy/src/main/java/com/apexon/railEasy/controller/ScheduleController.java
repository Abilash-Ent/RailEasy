package com.apexon.railEasy.controller;

import com.apexon.railEasy.constants.AppConstants;
import com.apexon.railEasy.constants.TravelClass;
import com.apexon.railEasy.dto.request.ScheduleRequest;
import com.apexon.railEasy.dto.response.ScheduleResponse;
import com.apexon.railEasy.dto.response.SeatAvailabilityResponse;
import com.apexon.railEasy.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Schedule search & seat availability (public) and schedule management (ADMIN).
 */
@Tag(name = "Schedules", description = "Train schedules, search and seat availability")
@RestController
@RequestMapping(AppConstants.SCHEDULE_BASE)
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @Operation(summary = "Search schedules by from/to station and journey date")
    @GetMapping
    public Flux<ScheduleResponse> search(@RequestParam String from,
                                         @RequestParam String to,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.search(from, to, date);
    }

    @Operation(summary = "List all schedules (admin browse)")
    @GetMapping("/all")
    public Flux<ScheduleResponse> getAll() {
        return scheduleService.getAll();
    }

    @Operation(summary = "List schedules for a given train (admin browse)")
    @GetMapping("/by-train/{trainId}")
    public Flux<ScheduleResponse> getByTrain(@PathVariable Long trainId) {
        return scheduleService.getByTrain(trainId);
    }

    @Operation(summary = "Get a schedule by id")
    @GetMapping("/{id}")
    public Mono<ScheduleResponse> getById(@PathVariable Long id) {
        return scheduleService.getById(id);
    }

    @Operation(summary = "Get the seat map (booked/available) for a schedule + travel class")
    @GetMapping("/{id}/seats")
    public Mono<SeatAvailabilityResponse> seats(@PathVariable Long id,
                                                @RequestParam("class") TravelClass travelClass) {
        return scheduleService.getSeatAvailability(id, travelClass);
    }

    @Operation(summary = "Create a schedule (ADMIN only)")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<ScheduleResponse> create(@Valid @RequestBody ScheduleRequest request) {
        return scheduleService.create(request);
    }

    @Operation(summary = "Update a schedule (ADMIN only)")
    @PutMapping("/{id}")
    public Mono<ScheduleResponse> update(@PathVariable Long id, @Valid @RequestBody ScheduleRequest request) {
        return scheduleService.update(id, request);
    }

    @Operation(summary = "Delete a schedule (ADMIN only)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable Long id) {
        return scheduleService.delete(id);
    }
}

