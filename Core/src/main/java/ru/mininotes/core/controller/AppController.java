package ru.mininotes.core.controller;

import jakarta.validation.Valid;
import org.apache.logging.log4j.util.PropertySource;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import ru.mininotes.core.domain.*;
import ru.mininotes.core.domain.requestEntity.*;
import ru.mininotes.core.repository.NoteRepository;
import ru.mininotes.core.repository.ProjectRepository;
import ru.mininotes.core.repository.UserRepository;


import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
//        if (principal.getName().equals(username) || userRepository.getUserByUsername(principal.getName()).get().getRole().equals(UserRole.ROLE_ADMIN)) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        Optional<User> loginUser = userRepository.getUserByUsername(principal.getName());
        if (optionalUser.isPresent()) {
            if (!optionalUser.get().getRelativeView(loginUser.get()).getProjectSet().isEmpty() ||
                    optionalUser.get().getUsername().equals(loginUser.get().getUsername())) {
                model.addAttribute("username", principal.getName());
                model.addAttribute("user", optionalUser.get().getRelativeView(loginUser.get()));
                model.addAttribute("loginUser", loginUser.get());
                return "user/workspace";
            } else {
                return "redirect:/profile/" + username;
            }
        } else {
            return "redirect:/error";
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
        if (userRepository.getUserByUsername(signUpRq.getUsername()).isPresent()){
            model.addAttribute("usernameError", "Пользователь с таким именем уже существует");
            return "signup";
        }
        if (userRepository.getUserByEmail(signUpRq.getEmail()).isPresent()){
            errors.rejectValue("email", "email.exist", "Пользователь с таким e-mail уже существует");
            return "signup";
        }
        if (!signUpRq.getPassword().equals(signUpRq.getPasswordConfirm())){
            errors.rejectValue("password", "password.no.match", "пароли не совпадают");
            errors.rejectValue("passwordConfirm", "passwordConfirm.no.match", "пароли не совпадают");
//            model.addAttribute("passwordError", "Пароли не совпадают");
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

    @GetMapping(value = "/{username}/project/add")
    public String getAddProjectPage(@PathVariable("username") String username, Principal principal, Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        Optional<User> loginUser = userRepository.getUserByUsername(principal.getName());
        if(!optionalUser.isPresent()) {
            return "redirect:/";
        } else if(principal.getName().equals(username) || loginUser.get().getRole().equals(UserRole.ROLE_ADMIN)) {
            model.addAttribute("username", principal.getName());
            model.addAttribute("user", optionalUser.get().getRelativeView(loginUser.get()));
            model.addAttribute("loginUser", loginUser.get());
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
        Optional<User> loginUser = userRepository.getUserByUsername(principal.getName());
        if(!optionalUser.isPresent()) {
            return "redirect:/error";
        } else if(principal.getName().equals(username) || userRepository.getUserByUsername(principal.getName()).get().getRole().equals(UserRole.ROLE_ADMIN)) {
            model.addAttribute("username", principal.getName());
            model.addAttribute("user", optionalUser.get().getRelativeView(loginUser.get()));
            model.addAttribute("loginUser", loginUser.get());
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
        User loginUser = userRepository.getUserByUsername(principal.getName()).get();
        if (optionalUser.isPresent()) {
            Optional<Project> optionalProject = optionalUser.get().getProjectSet().stream()
                    .filter(project -> project.getId() == projectId).findAny();
            if (optionalProject.isPresent()) {
                if (optionalProject.get().canEdit(loginUser)) {
                    User projectUser = optionalUser.get();
                    Project project = optionalProject.get();
                    model.addAttribute("username", principal.getName());
                    model.addAttribute("user", projectUser.getRelativeView(loginUser));
                    model.addAttribute("loginUser", loginUser);
                    model.addAttribute("project", project);
                    ProjectEditRq projectEditRq = new ProjectEditRq();
                    projectEditRq.setTitle(project.getTitle());
                    model.addAttribute("editProject", projectEditRq);
                    model.addAttribute("member", new ProjectAddMemberRq());
                    model.addAttribute("memberStatus", new ProjectChangeMemberStatusRq());
                    return "user/editProject";
                } else {
                    return "redirect:/error";
                }
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
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
        User loginUser = userRepository.getUserByUsername(principal.getName()).get();
        if (optionalUser.isPresent()) {
            Optional<Project> optionalProject = optionalUser.get().getProjectSet().stream()
                    .filter(project -> project.getId() == projectId).findAny();
            if (optionalProject.isPresent()) {
                if (optionalProject.get().canEdit(loginUser)) {
                    User projectUser = optionalUser.get();
                    Project project = optionalProject.get();
                    model.addAttribute("username", principal.getName());
                    model.addAttribute("user", projectUser.getRelativeView(loginUser));
                    model.addAttribute("loginUser", loginUser);
                    model.addAttribute("project", project);
                    model.addAttribute("member", new ProjectAddMemberRq());
                    model.addAttribute("memberStatus", new ProjectChangeMemberStatusRq());
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
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

    @PostMapping(value = "/{username}/project/{projectId}/delete")
    public String deleteProject(@PathVariable("username") String username,
                              @PathVariable("projectId") long projectId,
                              Principal principal,
                              Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        User loginUser = userRepository.getUserByUsername(principal.getName()).get();
        if (optionalUser.isPresent()) {
            Optional<Project> optionalProject = optionalUser.get().getProjectSet().stream()
                    .filter(project -> project.getId() == projectId).findAny();
            if (optionalProject.isPresent()) {
                if (optionalProject.get().canDelete(loginUser)) {
                    User projectUser = optionalUser.get();
                    Project project = optionalProject.get();
                    projectUser.removeProject(project);
                    projectUser.setDeletedNoteSet(projectUser.getDeletedNoteSet().stream().
                            filter(note -> project.getNoteSet().contains(note)).collect(Collectors.toSet()));
                    userRepository.save(projectUser);
                    return "redirect:/" + username;
                } else {
                    return "redirect:/error";
                }
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

    @GetMapping(value = "/{username}/project/{projectId}/addMember")
    public String getProjectSettingsPage(@PathVariable("username") String username,
                                         @PathVariable("projectId") long projectId,
                                         Model model) {
        return String.format("redirect:/%s/project/%s/settings", username, projectId);
    }

    @PostMapping(value = "/{username}/project/{projectId}/addMember")
    public String addMember(@PathVariable("username") String username,
                              @PathVariable("projectId") long projectId,
                              @ModelAttribute("member") @Valid ProjectAddMemberRq projectAddMemberRq,
                              Errors errors,
                              Principal principal,
                              Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        User loginUser = userRepository.getUserByUsername(principal.getName()).get();
        if (optionalUser.isPresent()) {
            Optional<Project> optionalProject = optionalUser.get().getProjectSet().stream()
                    .filter(project -> project.getId() == projectId).findAny();
            if (optionalProject.isPresent()) {
                if (optionalProject.get().canEdit(loginUser)) {
                    User projectUser = optionalUser.get();
                    Project project = optionalProject.get();
                    model.addAttribute("username", principal.getName());
                    model.addAttribute("user", projectUser.getRelativeView(loginUser));
                    model.addAttribute("loginUser", loginUser);
                    model.addAttribute("project", project);
                    ProjectEditRq projectEditRq = new ProjectEditRq();
                    projectEditRq.setTitle(project.getTitle());
                    model.addAttribute("editProject", projectEditRq);
                    model.addAttribute("memberStatus", new ProjectChangeMemberStatusRq());
                    if(errors.hasErrors()) {
                        return "user/editProject";
                    } else {
                        Optional<User> addUser = userRepository.getUserByUsername(projectAddMemberRq.getUsername());
                        if (addUser.isPresent()) {
                            if (!project.getSpectatorGroup().contains(addUser.get()) && !addUser.get().equals(project.getOwner())) {
                                project.getSpectatorGroup().add(addUser.get());
                                project.setLastUpdateDateTime(new Date());
                                projectRepository.save(project);
                                return String.format("redirect:/%s/project/%s/settings", username, project.getId());
                            } else {
                                errors.rejectValue("username", "user.exist", "Пользователь уже добавлен");
                                return "user/editProject";
                            }
                        } else {
                            errors.rejectValue("username", "user.no.exist", "Пользователь не найден");
                            return "user/editProject";
                        }
                    }
                } else {
                    return "redirect:/error";
                }
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

    @GetMapping(value = "/{username}/project/{projectId}/changeMemberStatus")
    public String getProjectSettingsPage2(@PathVariable("username") String username,
                                         @PathVariable("projectId") long projectId,
                                         Model model) {
        return String.format("redirect:/%s/project/%s/settings", username, projectId);
    }

    @PostMapping(value = "/{username}/project/{projectId}/changeMemberStatus")
    public String changeMemberStatus(@PathVariable("username") String username,
                            @PathVariable("projectId") long projectId,
                            @ModelAttribute("member") @Valid ProjectChangeMemberStatusRq projectChangeMemberStatusRq,
                            Errors errors,
                            Principal principal,
                            Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        User loginUser = userRepository.getUserByUsername(principal.getName()).get();
        if (optionalUser.isPresent()) {
            Optional<Project> optionalProject = optionalUser.get().getProjectSet().stream()
                    .filter(project -> project.getId() == projectId).findAny();
            if (optionalProject.isPresent()) {
                if (optionalProject.get().canEdit(loginUser)) {
                    User projectUser = optionalUser.get();
                    Project project = optionalProject.get();
                    model.addAttribute("username", principal.getName());
                    model.addAttribute("user", projectUser.getRelativeView(loginUser));
                    model.addAttribute("loginUser", loginUser);
                    model.addAttribute("project", project);
                    ProjectEditRq projectEditRq = new ProjectEditRq();
                    projectEditRq.setTitle(project.getTitle());
                    model.addAttribute("editProject", projectEditRq);
                    model.addAttribute("member", new ProjectAddMemberRq());

                    if(errors.hasErrors()) {
                        return "user/editProject";
                    } else {
                        Optional<User> optionalMember = userRepository.getUserByUsername(projectChangeMemberStatusRq.getUsername());
                        if (optionalMember.isPresent() && !optionalMember.get().equals(project.getOwner())) {
                            User member = optionalMember.get();
                            if (projectChangeMemberStatusRq.getUserRole().equals("editor")) {
                                project.getEditorGroup().add(member);
                                project.getModeratorGroup().add(member);
                                project.getSpectatorGroup().add(member);
                            } else if (projectChangeMemberStatusRq.getUserRole().equals("moderator")) {
                                project.getEditorGroup().remove(member);
                                project.getModeratorGroup().add(member);
                                project.getSpectatorGroup().add(member);
                            } else if (projectChangeMemberStatusRq.getUserRole().equals("spectator")){
                                project.getEditorGroup().remove(member);
                                project.getModeratorGroup().remove(member);
                                project.getSpectatorGroup().add(member);
                            }
                            project.setLastUpdateDateTime(new Date());
                            projectRepository.save(project);
                            return String.format("redirect:/%s/project/%s/settings", username, project.getId());
                        } else {
                            return "redirect:/error";
                        }
                    }
                } else {
                    return "redirect:/error";
                }
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

    @GetMapping(value = "/{username}/project/{projectId}/deleteMember/{memberName}")
    public String getProjectSettingsPage(@PathVariable("username") String username,
                                          @PathVariable("projectId") long projectId,
                                          @PathVariable("memberName") String memberName,
                                          Model model) {
        return String.format("redirect:/%s/project/%s/settings", username, projectId);
    }

    @PostMapping(value = "/{username}/project/{projectId}/deleteMember/{memberName}")
    public String excludeMember(@PathVariable("username") String username,
                                     @PathVariable("projectId") long projectId,
                                     @PathVariable("memberName") String memberName,
                                     Principal principal,
                                     Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        User loginUser = userRepository.getUserByUsername(principal.getName()).get();
        if (optionalUser.isPresent()) {
            Optional<Project> optionalProject = optionalUser.get().getProjectSet().stream()
                    .filter(project -> project.getId() == projectId).findAny();
            if (optionalProject.isPresent()) {
                if (optionalProject.get().canEdit(loginUser)) {
                    User projectUser = optionalUser.get();
                    Project project = optionalProject.get();
                    model.addAttribute("username", principal.getName());
                    model.addAttribute("user", projectUser.getRelativeView(loginUser));
                    model.addAttribute("loginUser", loginUser);
                    model.addAttribute("project", project);
                    ProjectEditRq projectEditRq = new ProjectEditRq();
                    projectEditRq.setTitle(project.getTitle());
                    model.addAttribute("editProject", projectEditRq);
                    model.addAttribute("member", new ProjectAddMemberRq());
                    model.addAttribute("memberStatus", new ProjectChangeMemberStatusRq());

                    if ((memberName != null) && (memberName.length() != 0)) {
                        Optional<User> optionalMember = userRepository.getUserByUsername(memberName);

                        if (optionalMember.isPresent() && !optionalMember.get().equals(project.getOwner())) {
                            User member = optionalMember.get();
                            project.getEditorGroup().remove(member);
                            project.getModeratorGroup().remove(member);
                            project.getSpectatorGroup().remove(member);
                            project.setLastUpdateDateTime(new Date());
                            projectRepository.save(project);

                            return String.format("redirect:/%s/project/%s/settings", username, project.getId());
                        } else {
                            return "redirect:/error";
                        }
                    } else {
                        return "redirect:/error";
                    }
                } else {
                    return "redirect:/error";
                }
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

    @GetMapping(value = "/{username}/project/{projectId}/note/add")
    public String getAddNotePage(@PathVariable("username") String username,
                                     @PathVariable("projectId") long projectId,
                                     Principal principal,
                                     Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        User loginUser = userRepository.getUserByUsername(principal.getName()).get();
        if (optionalUser.isPresent()) {
            Optional<Project> optionalProject = optionalUser.get().getProjectSet().stream()
                    .filter(project -> project.getId() == projectId).findAny();
            if (optionalProject.isPresent()) {
                if (optionalProject.get().canCreateNote(loginUser)) {
                    User projectUser = optionalUser.get();
                    Project project = optionalProject.get();
                    model.addAttribute("username", principal.getName());
                    model.addAttribute("user", projectUser.getRelativeView(loginUser));
                    model.addAttribute("loginUser", loginUser);
                    model.addAttribute("project", project);
                    model.addAttribute("note", new NoteRq());
                    return "user/addNote";
                } else {
                    return "redirect:/error";
                }
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
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
        User loginUser = userRepository.getUserByUsername(principal.getName()).get();
        if (optionalUser.isPresent()) {
            Optional<Project> optionalProject = optionalUser.get().getProjectSet().stream()
                    .filter(project -> project.getId() == projectId).findAny();
            if (optionalProject.isPresent()) {
                if(optionalProject.get().canCreateNote(loginUser)) {
                    model.addAttribute("username", principal.getName());
                    model.addAttribute("user", optionalUser.get().getRelativeView(loginUser));
                    model.addAttribute("loginUser", loginUser);
                    model.addAttribute("project", optionalProject.get());
                    if(errors.hasErrors()) {
                        return "user/addNote";
                    } else {
                        Project project = optionalProject.get();
                        Note note = new Note();
                        note.setTitle(noteRq.getTitle());
                        note.setContent(noteRq.getContent());
                        note.setHtml(markdownToHTML(noteRq.getContent()));
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
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

    @GetMapping(value = "/{username}/project/{projectId}/note/{noteId}")
    public String getNoteViewPage(@PathVariable("username") String username,
                                 @PathVariable("projectId") long projectId,
                                 @PathVariable("noteId") long noteId,
                                 Principal principal,
                                 Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        if (optionalUser.isPresent()) {
            Optional<Project> optionalProject = optionalUser.get().getProjectSet().stream()
                    .filter(project -> project.getId() == projectId).findAny();
            if (optionalProject.isPresent()) {
                Optional<Note> optionalNote = optionalProject.get().getNoteSet().stream()
                        .filter(note -> note.getId() == noteId).findAny();
                if (optionalNote.isPresent()) {
                    User loginUser = userRepository.getUserByUsername(principal.getName()).get();
                    if (optionalNote.get().canView(optionalProject.get(), loginUser)) {
                        User projectUser = optionalUser.get();
                        Project project = optionalProject.get();
                        model.addAttribute("username", principal.getName());
                        model.addAttribute("user", projectUser.getRelativeView(loginUser));
                        model.addAttribute("loginUser", loginUser);
                        model.addAttribute("project", project);
                        model.addAttribute("note", optionalNote.get());
                        model.addAttribute("canEdit", optionalNote.get().canEdit(project, loginUser));
                        return "user/viewNote";
                    } else {
                        return "redirect:/error";
                    }
                } else {
                    return "redirect:/error";
                }
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

    private String markdownToHTML(String markdown) {
        Parser parser = Parser.builder()
                .build();

        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder()
                .build();

        return renderer.render(document);
    }

    @GetMapping(value = "/{username}/project/{projectId}/note/{noteId}/edit")
    public String getNoteEditPage(@PathVariable("username") String username,
                                  @PathVariable("projectId") long projectId,
                                  @PathVariable("noteId") long noteId,
                                  Principal principal,
                                  Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        if (optionalUser.isPresent()) {
            Optional<Project> optionalProject = optionalUser.get().getProjectSet().stream()
                    .filter(project -> project.getId() == projectId).findAny();
            if (optionalProject.isPresent()) {
                Optional<Note> optionalNote = optionalProject.get().getNoteSet().stream()
                        .filter(note -> note.getId() == noteId).findAny();
                if (optionalNote.isPresent()) {
                    User loginUser = userRepository.getUserByUsername(principal.getName()).get();
                    if (optionalNote.get().canEdit(optionalProject.get(), loginUser)) {
                        User projectUser = optionalUser.get();
                        Project project = optionalProject.get();
                        Note note = optionalNote.get();
                        model.addAttribute("username", principal.getName());
                        model.addAttribute("user", projectUser.getRelativeView(loginUser));
                        model.addAttribute("loginUser", loginUser);
                        model.addAttribute("project", project);
                        model.addAttribute("note", note);
                        NoteEditRq noteEditRq = new NoteEditRq();
                        noteEditRq.setTitle(note.getTitle());
                        noteEditRq.setContent(note.getContent());
                        model.addAttribute("editNote", noteEditRq);
                        model.addAttribute("canDelete", optionalNote.get().canDelete(project, loginUser));
                        return "user/editNote";
                    } else {
                        return "redirect:/error";
                    }
                } else {
                    return "redirect:/error";
                }
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

    @PostMapping(value = "/{username}/project/{projectId}/note/{noteId}/edit")
    public String editNote(@PathVariable("username") String username,
                           @PathVariable("projectId") long projectId,
                           @PathVariable("noteId") long noteId,
                           @ModelAttribute("editNote") @Valid NoteEditRq noteEditRq,
                           Errors errors,
                           Principal principal,
                           Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        if (optionalUser.isPresent()) {
            Optional<Project> optionalProject = optionalUser.get().getProjectSet().stream()
                    .filter(project -> project.getId() == projectId).findAny();
            if (optionalProject.isPresent()) {
                Optional<Note> optionalNote = optionalProject.get().getNoteSet().stream()
                        .filter(note -> note.getId() == noteId).findAny();
                if (optionalNote.isPresent()) {
                    User loginUser = userRepository.getUserByUsername(principal.getName()).get();
                    if (optionalNote.get().canEdit(optionalProject.get(), loginUser)) {
                        User projectUser = optionalUser.get();
                        Project project = optionalProject.get();
                        model.addAttribute("username", principal.getName());
                        model.addAttribute("user", projectUser.getRelativeView(loginUser));
                        model.addAttribute("loginUser", loginUser);
                        model.addAttribute("project", project);
                        model.addAttribute("note", optionalNote.get());
                        model.addAttribute("canDelete", optionalNote.get().canDelete(project, loginUser));

                        if (errors.hasErrors()) {
                            return "user/editNote";
                        } else {
                            Note note = optionalNote.get();
                            note.setTitle(noteEditRq.getTitle());
                            note.setContent(noteEditRq.getContent());
                            note.setHtml(markdownToHTML(noteEditRq.getContent()));
                            note.setStatus(NoteStatus.ACTIVE);
                            note.setLastUpdateDateTime(new Date());
                            noteRepository.save(note);
                            return String.format("redirect:/%s/project/%s/note/%s", username, project.getId(), note.getId());
                        }
                    } else {
                        return "redirect:/error";
                    }
                } else {
                    return "redirect:/error";
                }
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

    @PostMapping(value = "/{username}/project/{projectId}/note/{noteId}/delete")
    public String deleteNote(@PathVariable("username") String username,
                           @PathVariable("projectId") long projectId,
                           @PathVariable("noteId") long noteId,
                           Principal principal,
                           Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        if (optionalUser.isPresent()) {
            Optional<Project> optionalProject = optionalUser.get().getProjectSet().stream()
                    .filter(project -> project.getId() == projectId).findAny();
            if (optionalProject.isPresent()) {
                Optional<Note> optionalNote = optionalProject.get().getNoteSet().stream()
                        .filter(note -> note.getId() == noteId).findAny();
                if (optionalNote.isPresent()) {
                    User loginUser = userRepository.getUserByUsername(principal.getName()).get();
                    if (optionalNote.get().canDelete(optionalProject.get(), loginUser)) {
                        User projectUser = optionalUser.get();
                        Project project = optionalProject.get();
                        model.addAttribute("username", principal.getName());
                        model.addAttribute("user", projectUser.getRelativeView(loginUser));
                        model.addAttribute("loginUser", loginUser);
                        model.addAttribute("project", project);
                        model.addAttribute("note", optionalNote.get());
                        model.addAttribute("canDelete", optionalNote.get().canDelete(project, loginUser));

                        Note note = optionalNote.get();
                        note.setStatus(NoteStatus.DELETED);
                        note.setLastUpdateDateTime(new Date());
                        note.getProject().getOwner().getDeletedNoteSet().add(note);
                        userRepository.save(note.getProject().getOwner());
                        noteRepository.save(note);

                        return "redirect:/" + username;
                    } else {
                        return "redirect:/error";
                    }
                } else {
                    return "redirect:/error";
                }
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

//    @GetMapping(value = "/user/{username}")
//    public String getProfile(Principal principal, Model model) {
//        if (principal == null) {
//            model.addAttribute("user", new SignUpRq());
//            return "signup";
//        } else {
//            return "redirect:/";
//        }
//        return "redirect:/";
//    }

    @GetMapping(value = "/{username}/bin")
    public String getBin(@PathVariable("username") String username, Principal principal, Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        User loginUser = userRepository.getUserByUsername(principal.getName()).get();
        if (optionalUser.isPresent() && principal.getName().equals(username)) {
            model.addAttribute("username", principal.getName());
            model.addAttribute("user", optionalUser.get());
            model.addAttribute("loginUser", loginUser);
            return "user/bin";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping(value = "/{username}/bin/note/{noteId}/recover")
    public String recoverNoteFromBin(@PathVariable("username") String username,
                         @PathVariable("noteId") long noteId,
                         Principal principal,
                         Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        User loginUser = userRepository.getUserByUsername(principal.getName()).get();
        if (optionalUser.isPresent() && principal.getName().equals(username)) {
            Optional<Note> optionalNote = noteRepository.findById(noteId);
            if (optionalNote.isPresent()) {
                User projectUser = optionalUser.get();
                Note note = optionalNote.get();

                projectUser.getDeletedNoteSet().remove(note);
                note.setStatus(NoteStatus.ACTIVE);
                userRepository.save(projectUser);
                noteRepository.save(note);
                model.addAttribute("username", principal.getName());
                model.addAttribute("user", optionalUser.get());
                model.addAttribute("loginUser", loginUser);

                return String.format("redirect:/%s/bin", projectUser.getUsername());
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

    @PostMapping(value = "/{username}/bin/note/{noteId}/delete")
    public String deleteNoteFromBin(@PathVariable("username") String username,
                         @PathVariable("noteId") long noteId,
                         Principal principal,
                         Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        if (optionalUser.isPresent() && principal.getName().equals(username)) {
            Optional<Note> optionalNote = noteRepository.findById(noteId);
            if (optionalNote.isPresent()) {
                User projectUser = optionalUser.get();
                Note note = optionalNote.get();

                projectUser.getDeletedNoteSet().remove(note);
                userRepository.save(projectUser);
                Project project = note.getProject();
                project.removeNote(note);
                projectRepository.save(project);
                model.addAttribute("username", principal.getName());
                model.addAttribute("user", optionalUser.get());

                return String.format("redirect:/%s/bin", optionalUser.get().getUsername());
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

    @GetMapping(value = "/admin/userList")
    public String getAdminUserListPage(@RequestParam(required = false, name = "page") Integer pageNumber,
                                       @RequestParam(required = false, name = "size") Integer pageSize,
                                       Principal principal,
                                       Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(principal.getName());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (pageNumber == null) {
                pageNumber = 0;
            }
            if (pageSize == null) {
                pageSize = 10;
            }

            if (user.getRole().equals(UserRole.ROLE_ADMIN)) {
                model.addAttribute("username", principal.getName());
                model.addAttribute("user", user);
                model.addAttribute("loginUser", user);
                if ((pageNumber < 0) || (pageSize < 1)) {
                    pageNumber = 0;
                    pageSize = 10;
                }
                Page<User> page = userRepository.findAll(PageRequest.of(pageNumber, pageSize));
                if (!page.hasContent()) {
                    pageNumber = 0;
                    pageSize = 10;
                    page = userRepository.findAll(PageRequest.of(pageNumber, pageSize));
                }
                model.addAttribute("hasPrevious", page.hasPrevious());
                model.addAttribute("hasNext", page.hasNext());
                model.addAttribute("currentPage", pageNumber);
                model.addAttribute("size", pageSize);
//                List<User> userList = page.toList().stream().
//                        filter(item -> !item.getUsername().equals(user.getUsername())).collect(Collectors.toList());
                List<User> userList = page.toList();

//                userList.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getUsername(), o2.getUsername()));

                model.addAttribute("userList", userList);
                return "admin/userList";
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

    @GetMapping(value = "/admin/editUser/{username}")
    public String getAdminEditUserPage(@PathVariable("username") String username,
                                       Principal principal,
                                       Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        Optional<User> optionalLoginUser = userRepository.getUserByUsername(principal.getName());
        if (optionalUser.isPresent() && optionalLoginUser.isPresent()) {
            User user = optionalUser.get();
            User loginUser = optionalLoginUser.get();

            if (loginUser.getRole().equals(UserRole.ROLE_ADMIN) && (loginUser.getStatus() != UserStatus.BANNED)) {
                model.addAttribute("username", principal.getName());
                model.addAttribute("user", user);
                model.addAttribute("loginUser", loginUser);
                UserEditRq editUser = new UserEditRq();
                editUser.setUsername(user.getUsername());
                editUser.setEmail(user.getEmail());
                editUser.setRole(user.getRole().toString());
                editUser.setStatus(user.getStatus().toString());
                model.addAttribute("editUser", editUser);
                return "admin/editUser";
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

    @PostMapping(value = "/admin/editUser/{username}")
    public String editUser(@PathVariable("username") String username,
                              @ModelAttribute("editUser") @Valid UserEditRq userEditRq,
                              Errors errors,
                              Principal principal,
                              Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        Optional<User> optionalLoginUser = userRepository.getUserByUsername(principal.getName());
        if (optionalUser.isPresent() && optionalLoginUser.isPresent()) {
            User user = optionalUser.get();
            User loginUser = optionalLoginUser.get();
            model.addAttribute("username", principal.getName());
            model.addAttribute("user", user);
            model.addAttribute("loginUser", loginUser);
            if (loginUser.getRole().equals(UserRole.ROLE_ADMIN) && (loginUser.getStatus() != UserStatus.BANNED)) {
                if(errors.hasErrors()) {
                    return "admin/editUser";
                } else {
                    if (!userRepository.getUserByUsername(userEditRq.getUsername()).isPresent() ||
                            userEditRq.getUsername().equals(user.getUsername())) {
                        user.setUsername(userEditRq.getUsername());
                        if (!userRepository.getUserByEmail(userEditRq.getEmail()).isPresent() ||
                                userEditRq.getEmail().equals(user.getEmail())) {
                            user.setEmail(userEditRq.getEmail());

                            if (userEditRq.getStatus().equals("ACTIVE")) {
                                user.setStatus(UserStatus.ACTIVE);
                            } else if (userEditRq.getStatus().equals("NOT_CONFIRMED")) {
                                user.setStatus(UserStatus.NOT_CONFIRMED);
                            } else if (userEditRq.getStatus().equals("BANNED")) {
                                user.setStatus(UserStatus.BANNED);
                            }

                            if (userEditRq.getRole().equals("user")) {
                                user.setRole(UserRole.ROLE_USER);
                            } else if (userEditRq.getRole().equals("admin")) {
                                user.setRole(UserRole.ROLE_ADMIN);
                            }

                            userRepository.save(user);

                            return "redirect:/admin/userList";
                        } else {
                            errors.rejectValue("email", "email.exist", "Пользователь с таким e-mail уже существует");
                            return "admin/editUser";
                        }
                    } else {
                        errors.rejectValue("username", "user.exist", "Пользователь с таким именем уже существует");
                        return "admin/editUser";
                    }
                }
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

    @PostMapping(value = "/admin/editUser/{username}/delete")
    public String deleteUser(@PathVariable("username") String username,
                           Principal principal,
                           Model model) {
        Optional<User> optionalUser = userRepository.getUserByUsername(username);
        Optional<User> optionalLoginUser = userRepository.getUserByUsername(principal.getName());
        if (optionalUser.isPresent() && optionalLoginUser.isPresent()) {
            User user = optionalUser.get();
            User loginUser = optionalLoginUser.get();
            if (loginUser.getRole().equals(UserRole.ROLE_ADMIN) && (loginUser.getStatus() != UserStatus.BANNED)) {
                userRepository.delete(user);

                return "redirect:/admin/userList";
            } else {
                return "redirect:/error";
            }
        } else {
            return "redirect:/error";
        }
    }

}
