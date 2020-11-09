package com.jm.task.controllers;


import com.jm.task.domain.User;
import com.jm.task.services.UsersService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;


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
    public String create(@ModelAttribute("newUser") @Valid User user, Errors errors, Model model, SessionStatus sessionStatus) {
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

    @GetMapping("/user")
    public String showUserUI() {
        return "mainPage";
    }

    @GetMapping(value = "/admin", params = "action=delete")
    public String deleteUser(@RequestParam("userId") long id) {
        usersService.delete(id);
        return "redirect:/users/admin/list";
    }

//    @GetMapping(value = "/admin", params = "action=showUpdateUserForm")
//    public String showUpdateUserForm(@RequestParam("userId") long id, Model model) {
//        UserDto userData = new UserDto(usersService.getUserById(id));
//        model.addAttribute("userData", userData);
//        return "updateUserForm";
//    }

//    @PostMapping(value = "/admin", params = "action=update")
//    public String updateUser(@ModelAttribute("userData") UserDto userDto, SessionStatus sessionStatus) {
//        usersService.update(userDto.getId(), usersService.createUserFromDto(userDto));
//        sessionStatus.setComplete();
//        return "redirect:/users/admin/list";
//    }

    @ExceptionHandler
    public String handleException(Exception ex, Model model, HttpSession session) {
        model.addAttribute("errorMessage", ex.getMessage());
        session.removeAttribute("newUserData");
        return "errorInfo";
    }

    @GetMapping("/authorizationFailure")
    public String accessDenied() {
        return "accessDenied";
    }
}