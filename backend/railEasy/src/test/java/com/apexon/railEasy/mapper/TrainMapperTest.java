package com.apexon.railEasy.mapper;

import com.apexon.railEasy.dto.request.TrainRequest;
import com.apexon.railEasy.dto.response.TrainResponse;
import com.apexon.railEasy.entity.Train;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class TrainMapperTest {

    private final TrainMapper mapper = Mappers.getMapper(TrainMapper.class);

    @Test
    void toEntity_mapsAllFields_andDefaultsActiveTrue() {
        TrainRequest request = TrainRequest.builder()
                .trainNumber("12621").trainName("Tamil Nadu Express").totalSeatsPerClass(64).build();

        Train entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getTrainNumber()).isEqualTo("12621");
        assertThat(entity.getTrainName()).isEqualTo("Tamil Nadu Express");
        assertThat(entity.getTotalSeatsPerClass()).isEqualTo(64);
        assertThat(entity.getActive()).isTrue();
    }

    @Test
    void updateEntity_overwritesEditableFields_keepsId() {
        Train train = Train.builder().id(7L).trainNumber("old").trainName("Old").totalSeatsPerClass(32).active(true).build();
        TrainRequest request = TrainRequest.builder()
                .trainNumber("22222").trainName("New Express").totalSeatsPerClass(64).build();

        mapper.updateEntity(train, request);

        assertThat(train.getId()).isEqualTo(7L);
        assertThat(train.getTrainNumber()).isEqualTo("22222");
        assertThat(train.getTrainName()).isEqualTo("New Express");
        assertThat(train.getTotalSeatsPerClass()).isEqualTo(64);
    }

    @Test
    void toResponse_mapsAllFields() {
        Train train = Train.builder().id(1L).trainNumber("12621").trainName("Tamil Nadu Express")
                .totalSeatsPerClass(64).active(true).build();

        TrainResponse response = mapper.toResponse(train);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTrainNumber()).isEqualTo("12621");
        assertThat(response.getTrainName()).isEqualTo("Tamil Nadu Express");
        assertThat(response.getTotalSeatsPerClass()).isEqualTo(64);
        assertThat(response.getActive()).isTrue();
    }
}
