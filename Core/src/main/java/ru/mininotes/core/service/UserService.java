package ru.mininotes.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mininotes.core.domain.User;
import ru.mininotes.core.repository.UserRepository;

import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

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

}
