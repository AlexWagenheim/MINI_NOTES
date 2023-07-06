package ru.mininotes.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.mininotes.core.domain.notification.Notification;
import ru.mininotes.core.domain.user.User;
import ru.mininotes.core.repository.NotificationRepository;
import ru.mininotes.core.repository.UserRepository;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private NotificationRepository notificationRepository;
    private UserRepository userRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void sendNotification(String username, String title, String content) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Notification notification = new Notification(title, content, user);
            notification.setHasRead(false);
            user.addNotification(notification);
            user.setHasNewNotification(true);
            userRepository.save(user);
        } else {
            logger.error(String.format("Попытка отправки уведомления несуществующему пользователю. Пользователь @%s не найден", username));
            throw new UsernameNotFoundException(String.format("Пользователь @%s не найден", username));
        }
    }

    public void setNotificationRead(String username, long notificationId) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Optional<Notification> optionalNotification = user.getNotificationList().stream()
                    .filter(notification -> notification.getId() == notificationId).findAny();
            if (optionalNotification.isPresent()) {
                Notification notification = optionalNotification.get();

                notification.setHasRead(true);
                notificationRepository.save(notification);
                System.out.println("================== " + user.getNotificationList().stream().filter(userNotification -> !userNotification.isHasRead()).toList().size());
                if (user.getNotificationList().stream().filter(userNotification -> !userNotification.isHasRead()).toList().size() <= 1) {
                    user.setHasNewNotification(false);
                }
//                (user.getNotificationList().stream().anyMatch(userNotification -> !userNotification.isHasRead()));

                userRepository.save(user);
            } else {
                logger.error(String.format("Попытка изменения состояния несуществующего уведомления. Уведомление (ID = %d) не найдено", notificationId));
                throw new UsernameNotFoundException(String.format("Уведомление (ID = %d) не найдено", notificationId));
            }
        } else {
            logger.error(String.format("Попытка изменения состояния уведомления у несуществующего пользователя. Пользователь @%s не найден", username));
            throw new UsernameNotFoundException(String.format("Пользователь @%s не найден", username));
        }
    }

    public void setAllNotificationsRead(String username) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            for (Notification notification: user.getNotificationList()) {
                notification.setHasRead(true);
            }
            user.setHasNewNotification(false);
            userRepository.save(user);
        } else {
            logger.error(String.format("Попытка изменения состояний уведомлений у несуществующего пользователя. Пользователь @%s не найден", username));
            throw new UsernameNotFoundException(String.format("Пользователь @%s не найден", username));
        }
    }

}
