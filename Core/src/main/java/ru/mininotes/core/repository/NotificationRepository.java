package ru.mininotes.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mininotes.core.domain.notification.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
