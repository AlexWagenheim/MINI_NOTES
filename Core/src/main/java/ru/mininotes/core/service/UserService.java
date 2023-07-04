package ru.mininotes.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import ru.mininotes.core.domain.User;
import ru.mininotes.core.mail.service.EmailService;
import ru.mininotes.core.repository.UserRepository;

import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private EmailService emailService;

    private UserRepository userRepository;

    private static final List<String> SEARCHABLE_FIELDS = Arrays.asList("username");

//    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> searchUsers(String text, List<String> fields, int limit) {

        List<String> fieldsToSearchBy = fields.isEmpty() ? SEARCHABLE_FIELDS : fields;

        boolean containsInvalidField = fieldsToSearchBy.stream().anyMatch(f -> !SEARCHABLE_FIELDS.contains(f));

        if(containsInvalidField) {
            throw new IllegalArgumentException();
        }

//        return userRepository.searchBy(
//                text, limit, fieldsToSearchBy.toArray(new String[0]));
        return null;
    }

    public boolean sendResetPasswordEmail(String email, String username, String key) {
//        System.out.printf("===== %s ===== Для пользователя %s был сброшен пароль. Ссылка для создания нового пароля: http://localhost:8080/resetPassword/%s", email, username, key);

        String message = String.format("Добрый день! \n\n" +
                "Вы получили это письмо, потому что для пользователя %s был запрошен сброс пароля на проекте MINI NOTES.\n\n" +
                "Ссылка для создания нового пароля: \n\n" +
                "http://localhost:8080/resetPassword/%s \n\n" +
                "Проект MINI NOTES", username, key);
        try {
            emailService.sendSimpleEmail(email, "Сброс пароля", message);
        } catch (MailException mailException) {
            logger.error("Error while sending out email..{}", mailException.getStackTrace());
            return false;
        }
        return true;
    }

}
