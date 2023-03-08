package com.example.testspring.api.controller.helpers;

import com.example.testspring.api.exceptions.NotFoundException;
import com.example.testspring.store.entity.ProjectEntity;
import com.example.testspring.store.entity.TaskStateEntity;
import com.example.testspring.store.repository.ProjectRepository;
import com.example.testspring.store.repository.TaskStateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Component
public class ControllerHelper {

    TaskStateRepository taskStateRepository;

    ProjectRepository projectRepository;

    @Transactional
    public ProjectEntity getProjectOrThrowExc(Long project_id) {
        return projectRepository.findById(project_id)
                .orElseThrow(() -> new NotFoundException("Project with " + project_id + " id doesn't exist."));
    }

    @Transactional
    public TaskStateEntity getTaskStateOrThrowExc(Long taskStateId){
        return taskStateRepository.findById(taskStateId)
                .orElseThrow(()-> new NotFoundException("Task state with "+ taskStateId+" id doesn't exist."));
    }
}
