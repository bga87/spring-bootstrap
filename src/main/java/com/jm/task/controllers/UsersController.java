package com.jm.task.controllers;


import com.jm.task.domain.User;
import com.jm.task.services.UsersService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import javax.validation.Valid;
import java.util.List;


@Controller
@RequestMapping
@SessionAttributes(names = {"availableRoles", "newUser", "users"})
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @ModelAttribute("newUser")
    public User newUser() {
        return new User();
    }

    @ModelAttribute("users")
    public List<User> users() {
        return usersService.listUsers();
    }

    @GetMapping("/admin")
    public String showAdminUI() {
        return "mainPage";
    }

    @PostMapping("/admin")
    public String createUser(@ModelAttribute("newUser") @Valid User user, Errors errors,
                             SessionStatus sessionStatus) {
        if (errors.hasErrors()) {
            return "mainPage";
        }
        usersService.save(user);
        sessionStatus.setComplete();
        return "redirect:/admin";
    }

    @DeleteMapping("/admin/{id}")
    public String deleteUser(@PathVariable("id") Long id, SessionStatus sessionStatus) {
        usersService.delete(id);
        sessionStatus.setComplete();
        return "redirect:/admin";
    }

    @PatchMapping("/admin/{id}")
    public String updateUser(@PathVariable("id") Long id, @ModelAttribute("updatedUser") @Valid User user,
                             Errors errors, Model model, SessionStatus sessionStatus) {
        if (errors.hasErrors()) {
            model.addAttribute("userId", id);
            return "mainPage";
        }
        usersService.update(id, user);
        sessionStatus.setComplete();
        return "redirect:/admin";
    }

    @GetMapping("/user")
    public String showUserUI() {
        return "mainPage";
    }

    @ExceptionHandler
    public String handleException(Exception ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "errorInfo";
    }

    @RequestMapping(path = "/accessDenied")
    public String accessDenied() {
        return "accessDenied";
    }

}