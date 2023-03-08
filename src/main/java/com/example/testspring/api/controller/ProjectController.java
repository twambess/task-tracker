package com.example.testspring.api.controller;

import com.example.testspring.api.controller.helpers.ControllerHelper;
import com.example.testspring.api.dto.AskDTO;
import com.example.testspring.api.dto.ProjectDTO;
import com.example.testspring.api.exceptions.BadRequestException;
import com.example.testspring.api.factories.ProjectDTOFactory;
import com.example.testspring.store.entity.ProjectEntity;
import com.example.testspring.store.repository.ProjectRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectController {

    ProjectRepository repository;

    ProjectDTOFactory dtoFactory;

    ControllerHelper helper;

    private static final String FETCH_PROJECT = "/api/projects";
    private static final String DELETE_PROJECT = "/api/projects/{project_id}";
    public static final String CREATE_OR_UPDATE_PROJECT = "/api/projects";

    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectDTO createOrUpdateProject(@RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
                                            @RequestParam(value = "projectName", required = false) Optional<String> optionalProjectName) {

        optionalProjectName = optionalProjectName.filter(name -> !name.trim().isEmpty());

        boolean isCreate = !optionalProjectId.isPresent();

        if (isCreate && !optionalProjectName.isPresent()) {
            throw new BadRequestException("Project name cant be emtpy");
        }

        final ProjectEntity project = optionalProjectId
                .map(helper::getProjectOrThrowExc)
                .orElseGet(() -> ProjectEntity.builder().build());


        optionalProjectName
                .ifPresent(projectName -> {
                    repository
                            .findByName(projectName)
                            .filter(anotherProj -> !Objects.equals(anotherProj.getId(), project.getId()))
                            .ifPresent(anotherProj -> {
                                throw new BadRequestException("Project " + projectName + " is already exists.");
                            });

                    project.setName(projectName);
                });

        final ProjectEntity saveProject = repository.saveAndFlush(project);


        return dtoFactory.makeProjectDTO(saveProject);
    }

    @Transactional
    @GetMapping(FETCH_PROJECT)
    public List<ProjectDTO> fetchProject(@RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(repository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(repository::streamAllBy);

        return projectStream.map(dtoFactory::makeProjectDTO)
                .collect(Collectors.toList());
    }

    @DeleteMapping(DELETE_PROJECT)
    public AskDTO deleteProject(@PathVariable(name = "project_id") Long project_id) {

        ProjectEntity entity = helper.getProjectOrThrowExc(project_id);

        repository.deleteById(project_id);

        return AskDTO.makeDefault(true);
    }


}
