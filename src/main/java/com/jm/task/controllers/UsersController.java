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

import javax.servlet.http.HttpSession;
import javax.validation.Valid;


@Controller
@RequestMapping
@SessionAttributes(names = {"newUser"})
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @ModelAttribute("newUser")
    public User getNewUser() {
        return new User();
    }

    @GetMapping("/admin")
    public String showAdminUI(Model model) {
        model.addAttribute("users", usersService.listUsers());
        return "mainPage";
    }

    @PostMapping("/admin")
    public String createUser(@ModelAttribute("newUser") @Valid User user, Errors errors, Model model, SessionStatus sessionStatus) {
        if (errors.hasErrors()) {
            model.addAttribute("users", usersService.listUsers());
            model.addAttribute("newUserValidationError", true);
            return "mainPage";
        }
        usersService.save(user);
        sessionStatus.setComplete();
        return "redirect:/admin";
    }

    @DeleteMapping("/admin/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        usersService.delete(id);
        return "redirect:/admin";
    }

    @PatchMapping("/admin")
    public String updateUser(@ModelAttribute("updatedUser") @Valid User user, Errors errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("users", usersService.listUsers());
            model.addAttribute("updatedUser", user);
            model.addAttribute("updateUserValidationError", true);
            return "mainPage";
        }
        usersService.update(user.getId(), user);    // refactor!
        return "redirect:/admin";
    }

    @GetMapping("/user")
    public String showUserUI() {
        return "mainPage";
    }

    @ExceptionHandler
    public String handleException(Exception ex, Model model, HttpSession session) {
        model.addAttribute("errorMessage", ex.getMessage());
        session.removeAttribute("newUser");
        return "errorInfo";
    }

    @RequestMapping(path = "/accessDenied")
    public String accessDenied() {
        return "accessDenied";
    }
}