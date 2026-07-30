package com.apexon.railEasy.service.impl;

import com.apexon.railEasy.dto.request.TrainRequest;
import com.apexon.railEasy.dto.response.TrainResponse;
import com.apexon.railEasy.entity.Train;
import com.apexon.railEasy.exception.DuplicateResourceException;
import com.apexon.railEasy.exception.ResourceNotFoundException;
import com.apexon.railEasy.mapper.TrainMapper;
import com.apexon.railEasy.repository.ScheduleRepository;
import com.apexon.railEasy.repository.TrainRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainServiceImplTest {

    @Mock
    private TrainRepository trainRepository;
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private TrainMapper trainMapper;

    @InjectMocks
    private TrainServiceImpl trainService;

    private TrainRequest request() {
        return TrainRequest.builder()
                .trainNumber("12621").trainName("Tamil Nadu Express").totalSeatsPerClass(64).build();
    }

    @Test
    void create_savesNewTrain() {
        TrainRequest request = request();
        Train entity = Train.builder().trainNumber("12621").trainName("Tamil Nadu Express")
                .totalSeatsPerClass(64).active(true).build();
        Train saved = Train.builder().id(1L).trainNumber("12621").trainName("Tamil Nadu Express")
                .totalSeatsPerClass(64).active(true).build();
        TrainResponse response = TrainResponse.builder().id(1L).trainNumber("12621").build();

        when(trainRepository.existsByTrainNumber("12621")).thenReturn(Mono.just(false));
        when(trainMapper.toEntity(request)).thenReturn(entity);
        when(trainRepository.save(entity)).thenReturn(Mono.just(saved));
        when(trainMapper.toResponse(saved)).thenReturn(response);

        StepVerifier.create(trainService.create(request))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void create_rejectsDuplicateTrainNumber() {
        TrainRequest request = request();
        when(trainRepository.existsByTrainNumber("12621")).thenReturn(Mono.just(true));

        StepVerifier.create(trainService.create(request))
                .expectError(DuplicateResourceException.class)
                .verify();
    }

    @Test
    void getById_returnsNotFoundWhenMissing() {
        when(trainRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(trainService.getById(99L))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void delete_returnsNotFoundWhenMissing() {
        when(trainRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(trainService.delete(99L))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}


