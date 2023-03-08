package com.example.testspring.store.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "task")
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private String name;
    @Builder.Default
    private Instant createdAt=Instant.now();
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TaskEntity that = (TaskEntity) o;
        return id != null && Objects.equals(id, that.id);
    }


    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
