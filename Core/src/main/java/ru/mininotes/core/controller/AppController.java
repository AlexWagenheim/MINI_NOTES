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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.mininotes.core.domain.*;
import ru.mininotes.core.domain.requestEntity.NoteRq;
import ru.mininotes.core.domain.requestEntity.ProjectEditRq;
import ru.mininotes.core.domain.requestEntity.ProjectRq;
import ru.mininotes.core.domain.requestEntity.SignUpRq;
import ru.mininotes.core.repository.NoteRepository;
import ru.mininotes.core.repository.ProjectRepository;
import ru.mininotes.core.repository.UserRepository;

import java.security.Principal;
import java.util.Date;
import java.util.Optional;

@Controller
public class AppController {

    private UserRepository userRepository;
    private ProjectRepository projectRepository;
    private NoteRepository noteRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AppController(UserRepository userRepository, ProjectRepository projectRepository, NoteRepository noteRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.noteRepository = noteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(value = "/")
    public String index(Principal principal, Model model) {
        if (principal == null) {
            return "index";
        } else {
//            model.addAttribute("username", principal.getName());
//            model.addAttribute("user", userRepository.getUserByUsername(principal.getName()).get());
            return "redirect:/" + principal.getName();
        }
    }

    @GetMapping(value = "/{username}")
    public String workspace(@PathVariable("username") String username, Principal principal, Model model) {
        if (principal.getName().equals(username) || userRepository.getUserByUsername(principal.getName()).get().getRole().equals(UserRole.ROLE_ADMIN)) {
            model.addAttribute("username", principal.getName());
            model.addAttribute("user", userRepository.getUserByUsername(username).get());
            return "user/workspace";
        } else {
            return "redirect:/";
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
            return "redirect:/" + principal.getName();
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

    @GetMapping(value = "/{username}/project/add")
    public String getAddProjectPage(@PathVariable("username") String username, Principal principal, Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        if(!optionalUser.isPresent()) {
            return "redirect:/";
        } else if(principal.getName().equals(username) || userRepository.getUserByUsername(principal.getName()).get().getRole().equals(UserRole.ROLE_ADMIN)) {
            model.addAttribute("username", principal.getName());
            model.addAttribute("user", optionalUser.get());
            model.addAttribute("project", new ProjectRq());
            return "user/addProject";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping(value = "/{username}/project/add")
    public String addProject(@PathVariable("username") String username,
                             @ModelAttribute("project") @Valid ProjectRq projectRq,
                             Errors errors,
                             Principal principal,
                             Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        if(!optionalUser.isPresent()) {
            return "redirect:/error";
        } else if(principal.getName().equals(username) || userRepository.getUserByUsername(principal.getName()).get().getRole().equals(UserRole.ROLE_ADMIN)) {
            model.addAttribute("username", principal.getName());
            model.addAttribute("user", optionalUser.get());
            if(errors.hasErrors()) {
                return "user/addProject";
            } else {
                User user = optionalUser.get();
                user.addProject(new Project(projectRq.getTitle(), new Date(), projectRq.getStatus().equals("public")));
                userRepository.save(user);
                return "redirect:/" + username;
            }
        } else {
            return "redirect:/";
        }
    }

    @GetMapping(value = "/{username}/project/{projectId}/settings")
    public String getEditProjectPage(@PathVariable("username") String username,
                                    @PathVariable("projectId") long projectId,
                                    Principal principal,
                                    Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (!optionalUser.isPresent() || !optionalProject.isPresent()) {
            return "redirect:/error";
        } else {
            User loginUser = userRepository.getUserByUsername(principal.getName()).get();
            User projectUser = optionalUser.get();
            Project project = optionalProject.get();
            if (project.getOwner().equals(loginUser) || project.getEditorGroup().contains(loginUser) || loginUser.getRole().equals(UserRole.ROLE_ADMIN)) {
                model.addAttribute("username", principal.getName());
                model.addAttribute("user", projectUser);
                model.addAttribute("project", project);
                model.addAttribute("editProject", new ProjectEditRq());
                return "user/editProject";
            } else {
                return "redirect:/error";
            }
        }
    }

    @PostMapping(value = "/{username}/project/{projectId}/settings")
    public String editProject(@PathVariable("username") String username,
                              @PathVariable("projectId") long projectId,
                              @ModelAttribute("editProject") @Valid ProjectEditRq projectEditRq,
                              Errors errors,
                              Principal principal,
                              Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (!optionalUser.isPresent() || !optionalProject.isPresent()) {
            return "redirect:/error";
        } else {
            User loginUser = userRepository.getUserByUsername(principal.getName()).get();
            User projectUser = optionalUser.get();
            Project project = optionalProject.get();
            if (project.getOwner().equals(loginUser) || project.getEditorGroup().contains(loginUser) || loginUser.getRole().equals(UserRole.ROLE_ADMIN)) {
                model.addAttribute("username", principal.getName());
                model.addAttribute("user", projectUser);
                model.addAttribute("project", project);
                if(errors.hasErrors()) {
                    return "user/editProject";
                } else {
                    project.setTitle(projectEditRq.getTitle());
                    project.setPublic(projectEditRq.getStatus().equals("public"));
                    project.setLastUpdateDateTime(new Date());
                    projectRepository.save(project);
                    return "redirect:/" + username;
                }
            } else {
                return "redirect:/error";
            }
        }
    }

    @PostMapping(value = "/{username}/project/{projectId}/delete")
    public String deleteProject(@PathVariable("username") String username,
                              @PathVariable("projectId") long projectId,
                              Principal principal,
                              Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (!optionalUser.isPresent() || !optionalProject.isPresent()) {
            return "redirect:/error";
        } else {
            User loginUser = userRepository.getUserByUsername(principal.getName()).get();
            User projectUser = optionalUser.get();
            Project project = optionalProject.get();
            if (project.getOwner().equals(loginUser) || project.getEditorGroup().contains(loginUser) || loginUser.getRole().equals(UserRole.ROLE_ADMIN)) {
                projectUser.removeProject(project);
                userRepository.save(projectUser);
                return "redirect:/" + username;
            } else {
                return "redirect:/error";
            }
        }
    }

    @GetMapping(value = "/{username}/project/{projectId}/note/add")
    public String getAddNotePage(@PathVariable("username") String username,
                                     @PathVariable("projectId") long projectId,
                                     Principal principal,
                                     Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (!optionalUser.isPresent() || !optionalProject.isPresent()) {
            return "redirect:/error";
        } else {
            User loginUser = userRepository.getUserByUsername(principal.getName()).get();
            User projectUser = optionalUser.get();
            Project project = optionalProject.get();
            if (project.getOwner().equals(loginUser) || project.getEditorGroup().contains(loginUser) || loginUser.getRole().equals(UserRole.ROLE_ADMIN)) {
                model.addAttribute("username", principal.getName());
                model.addAttribute("user", projectUser);
                model.addAttribute("project", project);
                model.addAttribute("note", new NoteRq());
                return "user/addNote";
            } else {
                return "redirect:/error";
            }
        }
    }

    @PostMapping(value = "/{username}/project/{projectId}/note/add")
    public String addNote(@PathVariable("username") String username,
                          @PathVariable("projectId") long projectId,
                          @ModelAttribute("note") @Valid NoteRq noteRq,
                          Errors errors,
                          Principal principal,
                          Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (!optionalUser.isPresent() || !optionalProject.isPresent()) {
            return "redirect:/error";
        } else if(principal.getName().equals(username) || userRepository.getUserByUsername(principal.getName()).get().getRole().equals(UserRole.ROLE_ADMIN)) {
            model.addAttribute("username", principal.getName());
            model.addAttribute("user", optionalUser.get());
            model.addAttribute("project", optionalProject.get());
            if(errors.hasErrors()) {
                return "user/addNote";
            } else {
                Project project = optionalProject.get();
                Note note = new Note();
                note.setTitle(noteRq.getTitle());
                note.setBody(noteRq.getBody());
                note.setStatus(NoteStatus.ACTIVE);
                Date date = new Date();
                note.setCreatedDateTime(date);
                note.setLastUpdateDateTime(date);
                project.addNote(note);
                projectRepository.save(project);
                return "redirect:/" + username;
            }
        } else {
            return "redirect:/error";
        }
    }
}
