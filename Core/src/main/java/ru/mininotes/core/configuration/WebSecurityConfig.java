package ru.mininotes.core.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.mininotes.core.domain.user.User;
import ru.mininotes.core.domain.user.UserStatus;
import ru.mininotes.core.repository.UserRepository;

import java.util.Optional;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/styles/**",
                                "/images/**",
                                "/node_modules/**",
                                "/signup",
                                "/pages/**",
                                "/resetPassword",
                                "/resetPassword/*",
                                "/").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .permitAll()
                )
//                .headers().frameOptions().disable();
                .logout()
                .logoutSuccessUrl("/");
//                .logout((logout) -> logout.permitAll());
        return http.build();
    }

//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails userDetails =
//                User.withDefaultPasswordEncoder()
//                        .username("admin")
//                        .password("password")
//                        .roles("ADMIN")
//                        .build();
//        return new InMemoryUserDetailsManager(userDetails);
//    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            Optional<User> user = userRepository.getUserByUsername(username);
            if (user.isPresent()) {
                if (!user.get().getStatus().equals(UserStatus.BANNED)) {
                    return user.get();
                } else {
                    throw new UsernameNotFoundException("Пользователь ‘" + username + "’ заблокирован");
                }
            } else {
                throw new UsernameNotFoundException("Пользователь ‘" + username + "’ не найден");
            }
        };
    }

}
