package com.example.testspring.api.factories;

import com.example.testspring.api.dto.ProjectDTO;
import com.example.testspring.store.entity.ProjectEntity;
import org.springframework.stereotype.Component;

@Component
public class ProjectDTOFactory {

    public ProjectDTO makeProjectDTO(ProjectEntity entity){
        return ProjectDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
