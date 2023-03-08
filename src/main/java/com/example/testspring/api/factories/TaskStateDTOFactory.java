package com.example.testspring.api.factories;

import com.example.testspring.api.dto.TaskStateDTO;
import com.example.testspring.store.entity.TaskStateEntity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class TaskStateDTOFactory {

    TaskDTOFactory factory;

    public TaskStateDTO makeStateDto(TaskStateEntity entity){
        return TaskStateDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .leftTaskStateId(entity.getLeftTaskState().map(TaskStateEntity::getId).orElse(null))
                .rightTaskStateId(entity.getRightTaskState().map(TaskStateEntity::getId).orElse(null))
                .tasks(entity
                        .getTasks()
                        .stream()
                        .map(factory::makeTaskDTO)
                        .collect(Collectors.toList()))
                .build();
    }
}
