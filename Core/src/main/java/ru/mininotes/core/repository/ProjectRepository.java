package ru.mininotes.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mininotes.core.domain.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
