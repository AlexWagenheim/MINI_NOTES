package ru.mininotes.core.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.mininotes.core.domain.Project;
import ru.mininotes.core.domain.User;
import ru.mininotes.core.domain.UserRole;
import ru.mininotes.core.domain.UserStatus;
import ru.mininotes.core.domain.requestEntity.ProjectRq;
import ru.mininotes.core.domain.requestEntity.SignUpRq;
import ru.mininotes.core.repository.UserRepository;

import java.security.Principal;
import java.util.Date;
import java.util.Optional;

@Controller
public class AppController {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AppController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(value = "/")
    public String index(Principal principal, Model model) {
        if (principal == null) {
            return "index";
        } else {
            model.addAttribute("username", principal.getName());
            model.addAttribute("user", userRepository.getUserByUsername(principal.getName()).get());
            return "user/workspace";
        }
    }

    @GetMapping(value = "/login")
    public String login(Principal principal, Model model) {
        if (principal == null) {
            return "login";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping(value = "/signup")
    public String signup(Principal principal, Model model) {
        if (principal == null) {
            model.addAttribute("user", new SignUpRq());
            return "signup";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping(value = "/signup")
    public String register(@ModelAttribute("user") @Valid SignUpRq signUpRq, Errors errors, Model model) {
        if (errors.hasErrors()) {
            return "signup";
        }
        if (!signUpRq.getPassword().equals(signUpRq.getPasswordConfirm())){
            model.addAttribute("passwordError", "Пароли не совпадают");
            return "signup";
        }
        if (userRepository.getUserByUsername(signUpRq.getUsername()).isPresent()){
            model.addAttribute("usernameError", "Пользователь с таким именем уже существует");
            return "signup";
        }
        userRepository.save(new User(
                signUpRq.getUsername(),
                signUpRq.getEmail(),
                passwordEncoder.encode(signUpRq.getPassword()),
                UserStatus.NOT_CONFIRMED, UserRole.ROLE_USER));

        return "redirect:/login";
    }

    //==================================================================================================================

    @GetMapping(value = "/user/{username}")
    public String getProfile(Principal principal, Model model) {
//        if (principal == null) {
//            model.addAttribute("user", new SignUpRq());
//            return "signup";
//        } else {
//            return "redirect:/";
//        }
        return "redirect:/";
    }

    @GetMapping(value = "/project/add")
    public String getAddProjectPage(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/error";
        } else {
            model.addAttribute("project", new ProjectRq());
            return "user/addProject";
        }
    }

    @PostMapping(value = "/project/add")
    public String addProject(@ModelAttribute("project") @Valid ProjectRq projectRq, Errors errors, Principal principal, Model model) {
        System.out.println(projectRq.getTitle());
        System.out.println(projectRq.getStatus());
        if(errors.hasErrors()) {
            return "user/addProject";
        } else {
            User user = userRepository.getUserByUsername(principal.getName()).get();
            user.addProject(new Project(projectRq.getTitle(), new Date(), projectRq.getStatus().equals("public")));
            userRepository.save(user);
            return "redirect:/";
        }
    }
}
