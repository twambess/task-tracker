package com.example.testspring.store.repository;

import com.example.testspring.store.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskEntityRepository extends JpaRepository<TaskEntity,Long> {
}
