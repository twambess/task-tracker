package com.example.testspring.api.factories;

import com.example.testspring.api.dto.TaskDTO;
import com.example.testspring.store.entity.TaskEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskDTOFactory {

    public TaskDTO makeTaskDTO(TaskEntity entity){
        return TaskDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
