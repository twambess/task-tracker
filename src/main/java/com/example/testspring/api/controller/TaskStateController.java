package com.example.testspring.api.controller;

import com.example.testspring.api.controller.helpers.ControllerHelper;
import com.example.testspring.api.dto.AskDTO;
import com.example.testspring.api.dto.TaskStateDTO;
import com.example.testspring.api.exceptions.BadRequestException;
import com.example.testspring.api.factories.TaskStateDTOFactory;
import com.example.testspring.store.entity.ProjectEntity;
import com.example.testspring.store.entity.TaskStateEntity;
import com.example.testspring.store.repository.TaskStateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
public class TaskStateController {

    TaskStateRepository repository;
    TaskStateDTOFactory dtoFactory;
    ControllerHelper helper;

    public static final String GET_TASK_STATES = "/api/projects/{project_id}/task-states";
    public static final String CREATE_TASK_STATE = "/api/project/{project_id}/task-states";
    public static final String UPDATE_TASK_STATE = "/api/task-states/{task_state_id}";
    public static final String CHANGE_TASK_STATE = "/api/task-states/{task_state_id}/change/position";
    public static final String DELETE_TASK_STATE = "/api/task-states/{task_state_id}/delete";

    @GetMapping(GET_TASK_STATES)
    public List<TaskStateDTO> getTaskState(@PathVariable(name = "project_id") Long projectId) {

        ProjectEntity project = helper.getProjectOrThrowExc(projectId);

        return project
                .getTaskStates()
                .stream()
                .map(dtoFactory::makeStateDto)
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_TASK_STATE)
    public TaskStateDTO createTaskState(
            @PathVariable(name = "project_id") Long projectId,
            @RequestParam(name = "task_state_name") String taskStatename) {

        if (taskStatename.trim().isEmpty()) {
            throw new BadRequestException("Task state can't be empty.");
        }

        ProjectEntity project = helper.getProjectOrThrowExc(projectId);
        Optional<TaskStateEntity> optionalTaskStateEntity = Optional.empty();
        for (TaskStateEntity taskState : project.getTaskStates()) {
            if (taskState.getName().equalsIgnoreCase(taskStatename)) {
                throw new BadRequestException("Task state " + taskStatename + " already exists.");
            }
            if (taskState.getRightTaskState().isEmpty()) {
                optionalTaskStateEntity = Optional.of(taskState);
                break;
            }
        }

        TaskStateEntity taskState = repository.saveAndFlush(
                TaskStateEntity.builder()
                        .name(taskStatename)
                        .project(project)
                        .build());

        optionalTaskStateEntity
                .ifPresent(anotherTaskState -> {
                    taskState.setLeftTaskState(anotherTaskState);
                    anotherTaskState.setRightTaskState(taskState);
                    repository.saveAndFlush(anotherTaskState);
                });

        final TaskStateEntity savedTaskState = repository.save(taskState);

        return dtoFactory.makeStateDto(savedTaskState);
    }

    @PatchMapping(UPDATE_TASK_STATE)
    public TaskStateDTO updateTaskState(
            @PathVariable(name = "task_state_id") Long taskStateId,
            @RequestParam(name = "task_state_name") String taskStatename) {

        if (taskStatename.trim().isEmpty()) {
            throw new BadRequestException("Task state can't be empty.");
        }

        TaskStateEntity taskState = helper.getTaskStateOrThrowExc(taskStateId);

        repository.findTaskStateEntityByProjectIdAndNameContainsIgnoreCase(
                        taskState.getProject().getId(),
                        taskStatename)
                .filter(anotherTaskState -> !anotherTaskState.getId().equals(taskStateId))
                .ifPresent(it -> {
                    throw new BadRequestException("Task State " + taskStatename + " already exist");
                });

        taskState.setName(taskStatename);
        taskState = repository.saveAndFlush(taskState);

        return dtoFactory.makeStateDto(taskState);
    }

    @PatchMapping(CHANGE_TASK_STATE)
    public TaskStateDTO changeTaskStatePosition(
            @PathVariable(name = "task_state_id") Long taskStateId,
            @RequestParam(name = "left_task_state_id", required = false) Optional<Long> optionalLeftTaskStateId) {

        TaskStateEntity changeTaskState = helper.getTaskStateOrThrowExc(taskStateId);
        ProjectEntity project = changeTaskState.getProject();


        Optional<Long> oldLeftTaskStateId = changeTaskState
                .getLeftTaskState()
                .map(TaskStateEntity::getId);

        if (oldLeftTaskStateId.equals(optionalLeftTaskStateId)) {
            return dtoFactory.makeStateDto(changeTaskState);
        }

        Optional<TaskStateEntity> optionalNewLeftTaskState = optionalLeftTaskStateId
                .map(leftTaskStateId -> {

                    if (taskStateId.equals(leftTaskStateId)) {
                        throw new BadRequestException("Left task state id equals changed task state.");
                    }

                    TaskStateEntity leftTaskStateEntity = helper.getTaskStateOrThrowExc(leftTaskStateId);

                    if (!project.getId().equals(leftTaskStateEntity.getProject().getId())) {
                        throw new BadRequestException("Task state position can't be changed within the same project");
                    }

                    return leftTaskStateEntity;
                });

        Optional<TaskStateEntity> optionalNewRightTaskState;

        if (optionalNewLeftTaskState.isEmpty()) {
            optionalNewRightTaskState = project
                    .getTaskStates()
                    .stream()
                    .filter(anotherTaskState -> anotherTaskState.getLeftTaskState().isEmpty())
                    .findAny();
        } else {
            optionalNewRightTaskState = optionalNewLeftTaskState
                    .get()
                    .getRightTaskState();
        }

        replaceOldTaskStatesPosition(changeTaskState);

        if (optionalNewLeftTaskState.isPresent()) {
            TaskStateEntity newLeftTaskState = optionalNewLeftTaskState.get();

            newLeftTaskState.setRightTaskState(changeTaskState);

            changeTaskState.setLeftTaskState(newLeftTaskState);

        } else {
            changeTaskState.setLeftTaskState(null);
        }

        if (optionalNewRightTaskState.isPresent()) {
            TaskStateEntity newRightTaskState = optionalNewRightTaskState.get();

            newRightTaskState.setLeftTaskState(changeTaskState);

            changeTaskState.setRightTaskState(newRightTaskState);

        } else {
            changeTaskState.setLeftTaskState(null);
        }
        changeTaskState = repository.saveAndFlush(changeTaskState);

        optionalNewRightTaskState
                .ifPresent(repository::saveAndFlush);

        optionalNewLeftTaskState
                .ifPresent(repository::saveAndFlush);

        return dtoFactory.makeStateDto(changeTaskState);
    }

    @DeleteMapping(DELETE_TASK_STATE)
    public AskDTO deleteTaskState(
            @PathVariable(name = "task_state_id") Long taskStateId) {

        TaskStateEntity changeTaskState = helper.getTaskStateOrThrowExc(taskStateId);

        replaceOldTaskStatesPosition(changeTaskState);

        changeTaskState = repository.saveAndFlush(changeTaskState);
        repository.delete(changeTaskState);

        return AskDTO.makeDefault(true);
    }

    private void replaceOldTaskStatesPosition(TaskStateEntity changeTaskState) {
        Optional<TaskStateEntity> optionalOldLeftTaskState = changeTaskState.getLeftTaskState();
        Optional<TaskStateEntity> optionalOldRightTaskState = changeTaskState.getRightTaskState();

        optionalOldLeftTaskState
                .ifPresent(it -> {
                    it.setRightTaskState(optionalOldRightTaskState.orElse(null));

                    repository.saveAndFlush(it);
                });

        optionalOldRightTaskState
                .ifPresent(it -> {
                    it.setLeftTaskState(optionalOldLeftTaskState.orElse(null));
                    repository.saveAndFlush(it);
                });
    }
}
