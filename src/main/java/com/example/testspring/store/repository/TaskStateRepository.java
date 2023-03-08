package com.example.testspring.store.repository;

import com.example.testspring.store.entity.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskStateRepository extends JpaRepository<TaskStateEntity,Long> {

    Optional<TaskStateEntity> findTaskStateEntityByProjectIdAndNameContainsIgnoreCase(Long projectId, String taskStateName);
}
