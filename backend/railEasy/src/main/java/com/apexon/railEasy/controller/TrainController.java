package com.apexon.railEasy.controller;

import com.apexon.railEasy.constants.AppConstants;
import com.apexon.railEasy.dto.request.TrainRequest;
import com.apexon.railEasy.dto.response.TrainResponse;
import com.apexon.railEasy.service.TrainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Train management (ADMIN) and search/read (authenticated) endpoints.
 */
@Tag(name = "Trains", description = "Train management and search")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping(AppConstants.TRAIN_BASE)
public class TrainController {

    private final TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    @Operation(summary = "Create a new train (ADMIN only)")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<TrainResponse> create(@Valid @RequestBody TrainRequest request) {
        return trainService.create(request);
    }

    @Operation(summary = "Update an existing train (ADMIN only)")
    @PutMapping("/{id}")
    public Mono<TrainResponse> update(@PathVariable Long id, @Valid @RequestBody TrainRequest request) {
        return trainService.update(id, request);
    }

    @Operation(summary = "Delete a train (ADMIN only)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable Long id) {
        return trainService.delete(id);
    }

    @Operation(summary = "Get a train by id")
    @GetMapping("/{id}")
    public Mono<TrainResponse> getById(@PathVariable Long id) {
        return trainService.getById(id);
    }

    @Operation(summary = "List all trains")
    @GetMapping
    public Flux<TrainResponse> getAll() {
        return trainService.getAll();
    }
}

