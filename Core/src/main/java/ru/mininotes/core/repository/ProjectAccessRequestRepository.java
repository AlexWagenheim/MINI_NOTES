package ru.mininotes.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mininotes.core.domain.project.ProjectAccessRequest;

public interface ProjectAccessRequestRepository extends JpaRepository<ProjectAccessRequest, Long> {

}
