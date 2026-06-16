package com.mipt.mvpmts2.mapper;

import com.mipt.mvpmts2.dto.TaskCreateDto;
import com.mipt.mvpmts2.dto.TaskResponseDto;
import com.mipt.mvpmts2.dto.TaskUpdateDto;
import com.mipt.mvpmts2.model.Task;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedDate", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  @Mapping(target = "completed", constant = "false")
  Task toEntity(TaskCreateDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastModifiedDate", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  Task updateEntity(TaskUpdateDto dto, @MappingTarget Task task);

  TaskResponseDto toResponseDto(Task task);
}
