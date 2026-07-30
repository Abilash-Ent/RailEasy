package com.apexon.railEasy.mapper;

import com.apexon.railEasy.dto.request.TrainRequest;
import com.apexon.railEasy.dto.response.TrainResponse;
import com.apexon.railEasy.entity.Train;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Maps between {@link Train} entities and their DTO representations.
 */
@Mapper
public interface TrainMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    Train toEntity(TrainRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(@MappingTarget Train train, TrainRequest request);

    TrainResponse toResponse(Train train);
}



