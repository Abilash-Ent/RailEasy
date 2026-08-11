package com.apexon.railEasy.service.impl;

import com.apexon.railEasy.cache.TrainCache;
import com.apexon.railEasy.dto.request.TrainRequest;
import com.apexon.railEasy.dto.response.TrainResponse;
import com.apexon.railEasy.exception.BusinessValidationException;
import com.apexon.railEasy.exception.DuplicateResourceException;
import com.apexon.railEasy.exception.ResourceNotFoundException;
import com.apexon.railEasy.mapper.TrainMapper;
import com.apexon.railEasy.repository.ScheduleRepository;
import com.apexon.railEasy.repository.TrainRepository;
import com.apexon.railEasy.service.TrainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Default {@link TrainService} implementation.
 */
@Slf4j
@Service
public class TrainServiceImpl implements TrainService {

    private static final String TRAIN_NOT_FOUND =
            "We couldn't find the train you're looking for. It may have been removed.";

    private final TrainRepository trainRepository;
    private final ScheduleRepository scheduleRepository;
    private final TrainCache trainCache;
    private final TrainMapper trainMapper;

    public TrainServiceImpl(TrainRepository trainRepository,
                            ScheduleRepository scheduleRepository,
                            TrainCache trainCache,
                            TrainMapper trainMapper) {
        this.trainRepository = trainRepository;
        this.scheduleRepository = scheduleRepository;
        this.trainCache = trainCache;
        this.trainMapper = trainMapper;
    }

    @Override
    public Mono<TrainResponse> create(TrainRequest request) {
        return trainRepository.existsByTrainNumber(request.getTrainNumber())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new DuplicateResourceException(
                                "A train with number " + request.getTrainNumber()
                                        + " already exists. Please use a different train number."));
                    }
                    return trainRepository.save(trainMapper.toEntity(request));
                })
                .map(trainMapper::toResponse)
                .doOnSuccess(t -> {
                    trainCache.evictList();
                    log.info("Created train: {}", request.getTrainNumber());
                });
    }

    @Override
    public Mono<TrainResponse> update(Long id, TrainRequest request) {
        return trainRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(TRAIN_NOT_FOUND)))
                .flatMap(train -> {
                    trainMapper.updateEntity(train, request);
                    return trainRepository.save(train);
                })
                .map(trainMapper::toResponse)
                .doOnSuccess(t -> {
                    trainCache.evict(id);
                    log.info("Updated train id: {}", id);
                });
    }

    @Override
    public Mono<Void> delete(Long id) {
        return trainRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(TRAIN_NOT_FOUND)))
                .flatMap(train -> scheduleRepository.existsByTrainId(id)
                        .flatMap(hasSchedules -> Boolean.TRUE.equals(hasSchedules)
                                ? Mono.<Void>error(new BusinessValidationException(
                                        "This train has active schedules and cannot be deleted. "
                                                + "Please remove its schedules first."))
                                : trainRepository.delete(train)))
                .doOnSuccess(v -> {
                    trainCache.evict(id);
                    log.info("Deleted train id: {}", id);
                });
    }

    @Override
    public Mono<TrainResponse> getById(Long id) {
        return trainCache.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(TRAIN_NOT_FOUND)))
                .map(trainMapper::toResponse);
    }

    @Override
    public Flux<TrainResponse> getAll() {
        return trainCache.findAll(trainRepository::findAll).map(trainMapper::toResponse);
    }
}
