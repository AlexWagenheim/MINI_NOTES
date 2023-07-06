package ru.mininotes.core.service;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.mininotes.core.domain.notification.Notification;
import ru.mininotes.core.domain.project.Project;
import ru.mininotes.core.domain.project.ProjectAccessRequest;
import ru.mininotes.core.domain.project.ProjectAccessRequestType;
import ru.mininotes.core.domain.user.User;
import ru.mininotes.core.repository.ProjectAccessRequestRepository;
import ru.mininotes.core.repository.ProjectRepository;
import ru.mininotes.core.repository.UserRepository;

import java.util.Optional;

@Service
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private UserRepository userRepository;
    private ProjectRepository projectRepository;
    private NotificationService notificationService;
    private ProjectAccessRequestRepository requestRepository;

    @Autowired
    public ProjectService(UserRepository userRepository, ProjectRepository projectRepository, NotificationService notificationService, ProjectAccessRequestRepository requestRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.notificationService = notificationService;
        this.requestRepository = requestRepository;
    }

    public void acceptRequest(long  projectId, long requestId) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent()) {
            Project project = optionalProject.get();
            Optional<ProjectAccessRequest> optionalProjectAccessRequest = project.getProjectAccessRequestList().stream()
                    .filter(request -> request.getId() == requestId).findAny();
            if (optionalProjectAccessRequest.isPresent()) {
                ProjectAccessRequest projectAccessRequest = optionalProjectAccessRequest.get();
                if (projectAccessRequest.isActive()) {
                    Optional<User> optionalUser = userRepository.getUserByUsername(projectAccessRequest.getUsername());
                    if (optionalUser.isPresent()) {
                        User user = optionalUser.get();
                        switch (projectAccessRequest.getRequestType()) {
                            case GRANT_EDITOR_RIGHTS:
                                project.getEditorGroup().add(user);
                            case GRANT_MODERATOR_RIGHTS:
                                project.getModeratorGroup().add(user);
                            case GRANT_SPECTATOR_RIGHTS:
                                project.getSpectatorGroup().add(user);
                        }
                        projectAccessRequest.setActive(false);
                        project.removeAccessRequest(projectAccessRequest);
                        projectRepository.save(project);
                    } else {
                        logger.error(String.format("Попытка добавления несуществующего пользователя в проект (ID = %d). Пользователь @%s не найден", projectId, projectAccessRequest.getUsername()));
                        throw new UsernameNotFoundException(String.format("Пользователь @%s не найден", projectAccessRequest.getUsername()));
                    }
                } else {
                    logger.error(String.format("Попытка повторного выполнения запроса у проекта (ID = %d). Запрос (ID = %d) уже был обработан", projectId, requestId));
                }
            } else {
                logger.error(String.format("Попытка выполнения несуществующего запроса у проекта (ID = %d). Запрос (ID = %d) не найден", projectId, requestId));
                throw new EntityNotFoundException(String.format("Проект @%d не найден", projectId));
            }
        } else {
            logger.error(String.format("Попытка выполнения запроса у несуществующего проекта. Проект (ID = %d) не найден", projectId));
            throw new EntityNotFoundException(String.format("Проект (ID = %d) не найден", projectId));
        }
    }

    public void denyRequest(long  projectId, long requestId) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent()) {
            Project project = optionalProject.get();
            Optional<ProjectAccessRequest> optionalProjectAccessRequest = project.getProjectAccessRequestList().stream()
                    .filter(request -> request.getId() == requestId).findAny();
            if (optionalProjectAccessRequest.isPresent()) {
                ProjectAccessRequest projectAccessRequest = optionalProjectAccessRequest.get();
                if (projectAccessRequest.isActive()) {
                    projectAccessRequest.setActive(false);
                    project.removeAccessRequest(projectAccessRequest);
                    projectRepository.save(project);
                } else {
                    logger.error(String.format("Попытка повторного выполнения запроса у проекта (ID = %d). Запрос (ID = %d) уже был обработан", projectId, requestId));
                }
            } else {
                logger.error(String.format("Попытка выполнения несуществующего запроса у проекта (ID = %d). Запрос (ID = %d) не найден", projectId, requestId));
                throw new EntityNotFoundException(String.format("Проект @%d не найден", projectId));
            }
        } else {
            logger.error(String.format("Попытка выполнения запроса у несуществующего проекта. Проект (ID = %d) не найден", projectId));
            throw new EntityNotFoundException(String.format("Проект (ID = %d) не найден", projectId));
        }
    }

    public void makeRequest(long  projectId, String username, ProjectAccessRequestType requestType) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent()) {
            Optional<User> optionalUser = userRepository.getUserByUsername(username);
            Project project = optionalProject.get();
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                if (!hasActiveRequests(projectId, username, requestType)) {
                    ProjectAccessRequest projectAccessRequest =
                            new ProjectAccessRequest(requestType, username);
                    project.addAccessRequest(projectAccessRequest);

                    String title = "";
                    String content = "";
                    if (requestType.equals(ProjectAccessRequestType.GRANT_SPECTATOR_RIGHTS)) {
                        title = "Запрос на добавление в проект";
                        content = String.format("Пользователь %s просит добавить его в проект %s", username, project.getTitle());
                    } else if (requestType.equals(ProjectAccessRequestType.GRANT_MODERATOR_RIGHTS)) {
                        title = "Запрос на выдачу прав доступа";
                        content = String.format("Пользователь %s просит сделать его модератором в проекте %s", username, project.getTitle());
                    } else {
                        title = "Запрос на выдачу прав доступа";
                        content = String.format("Пользователь %s просит сделать его редактором в проекте %s", username, project.getTitle());
                    }
                    notificationService.sendNotification(project.getOwner().getUsername(), title, content);
                    for (User projectUser: project.getEditorGroup()) {
                        notificationService.sendNotification(projectUser.getUsername(), title, content);
                    }

                    projectRepository.save(project);
                } else {
                    logger.error(String.format("Попытка повторной отправки запроса от пользователя @%s в проект (ID = %d)", username, projectId));
                }
            } else {
                logger.error(String.format("Пользователь @%s не найден", username));
                throw new UsernameNotFoundException(String.format("Пользователь @%s не найден", username));
            }
        } else {
            logger.error(String.format("Попытка отправки запроса на добавление в несуществующий проект. Проект (ID = %d) не найден", projectId));
            throw new EntityNotFoundException(String.format("Проект (ID = %d) не найден", projectId));
        }
    }

    public boolean hasActiveRequests(long  projectId, String username, ProjectAccessRequestType projectAccessRequestType) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent()) {
            Optional<User> optionalUser = userRepository.getUserByUsername(username);
            Project project = optionalProject.get();
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                return project.getProjectAccessRequestList().stream()
                        .filter(request -> request.getUsername().equals(user.getUsername()))
                        .filter(ProjectAccessRequest::isActive)
                        .anyMatch(request -> request.getRequestType().equals(projectAccessRequestType));
            } else {
                logger.error(String.format("Пользователь @%s не найден", username));
                throw new UsernameNotFoundException(String.format("Пользователь @%s не найден", username));
            }
        } else {
            logger.error(String.format("Попытка взаимодействия с несуществующим проектом. Проект (ID = %d) не найден", projectId));
            throw new EntityNotFoundException(String.format("Проект (ID = %d) не найден", projectId));
        }
    }
}
