package ru.mininotes.core;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.mininotes.core.domain.user.User;
import ru.mininotes.core.domain.user.UserRole;
import ru.mininotes.core.index.Indexer;
import ru.mininotes.core.repository.UserRepository;

import java.util.Optional;

@EnableJpaRepositories("ru.mininotes.core.*")
@ComponentScan(basePackages = { "ru.mininotes.core.*" })
@EntityScan("ru.mininotes.core.*")
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    // Временное решение
    @Bean
    public ApplicationRunner createAdmin(UserRepository userRepository) throws Exception {
        return (ApplicationArguments args) -> {
            Optional<User> optionalUser = userRepository.getUserByUsername("admin");
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setRole(UserRole.ROLE_ADMIN);
                userRepository.save(user);
            }
        };
    }

    @Bean
    public ApplicationRunner buildIndex(Indexer indexer) throws Exception {
        return (ApplicationArguments args) -> {
            indexer.indexPersistedData("ru.mininotes.core.domain.user.User");
        };
    }
}
