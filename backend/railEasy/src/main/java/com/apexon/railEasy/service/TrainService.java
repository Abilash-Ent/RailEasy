package com.apexon.railEasy.service;

import com.apexon.railEasy.dto.request.TrainRequest;
import com.apexon.railEasy.dto.response.TrainResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Train management and search operations.
 */
public interface TrainService {

    Mono<TrainResponse> create(TrainRequest request);

    Mono<TrainResponse> update(Long id, TrainRequest request);

    Mono<Void> delete(Long id);

    Mono<TrainResponse> getById(Long id);

    Flux<TrainResponse> getAll();
}

