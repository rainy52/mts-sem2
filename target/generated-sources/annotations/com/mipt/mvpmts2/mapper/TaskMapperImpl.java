package com.mipt.mvpmts2.mapper;

import com.mipt.mvpmts2.dto.TaskCreateDto;
import com.mipt.mvpmts2.dto.TaskResponseDto;
import com.mipt.mvpmts2.dto.TaskUpdateDto;
import com.mipt.mvpmts2.model.Task;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-16T19:40:05+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Homebrew)"
)
@Component
public class TaskMapperImpl implements TaskMapper {

    @Override
    public Task toEntity(TaskCreateDto dto) {
        if ( dto == null ) {
            return null;
        }

        Task task = new Task();

        task.setTitle( dto.getTitle() );
        task.setDescription( dto.getDescription() );
        task.setDueDate( dto.getDueDate() );
        task.setPriority( dto.getPriority() );
        Set<String> set = dto.getTags();
        if ( set != null ) {
            task.setTags( new LinkedHashSet<String>( set ) );
        }

        task.setCompleted( false );

        return task;
    }

    @Override
    public Task updateEntity(TaskUpdateDto dto, Task task) {
        if ( dto == null ) {
            return task;
        }

        if ( dto.getTitle() != null ) {
            task.setTitle( dto.getTitle() );
        }
        if ( dto.getDescription() != null ) {
            task.setDescription( dto.getDescription() );
        }
        if ( dto.getCompleted() != null ) {
            task.setCompleted( dto.getCompleted() );
        }
        if ( dto.getDueDate() != null ) {
            task.setDueDate( dto.getDueDate() );
        }
        if ( dto.getPriority() != null ) {
            task.setPriority( dto.getPriority() );
        }
        if ( task.getTags() != null ) {
            Set<String> set = dto.getTags();
            if ( set != null ) {
                task.getTags().clear();
                task.getTags().addAll( set );
            }
        }
        else {
            Set<String> set = dto.getTags();
            if ( set != null ) {
                task.setTags( new LinkedHashSet<String>( set ) );
            }
        }

        return task;
    }

    @Override
    public TaskResponseDto toResponseDto(Task task) {
        if ( task == null ) {
            return null;
        }

        TaskResponseDto taskResponseDto = new TaskResponseDto();

        taskResponseDto.setId( task.getId() );
        taskResponseDto.setTitle( task.getTitle() );
        taskResponseDto.setDescription( task.getDescription() );
        taskResponseDto.setCompleted( task.isCompleted() );
        taskResponseDto.setCreatedAt( task.getCreatedAt() );
        taskResponseDto.setDueDate( task.getDueDate() );
        taskResponseDto.setPriority( task.getPriority() );
        Set<String> set = task.getTags();
        if ( set != null ) {
            taskResponseDto.setTags( new LinkedHashSet<String>( set ) );
        }

        return taskResponseDto;
    }
}
