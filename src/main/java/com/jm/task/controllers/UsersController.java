package com.jm.task.controllers;


import com.jm.task.domain.User;
import com.jm.task.dto.UserDto;
import com.jm.task.services.UsersService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import java.util.List;


@Controller
@RequestMapping("/users")
@SessionAttributes(names = {"newUserData", "userData"})
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @ModelAttribute("newUserData")
    public UserDto getNewUserData() {
        return new UserDto();
    }

    @GetMapping("/admin")
    public String adminAccess() {
        return "redirect:/users/admin/list";
    }

    @GetMapping("/user/{userId}")
    public String userAccess(@PathVariable("userId") long id) {
        return "redirect:/users/user/show/" + id;
    }

    @GetMapping("/admin/list")
    @ModelAttribute("users")
    public List<User> listUsers() {
        return usersService.listUsers();
    }

    @GetMapping("/user/show/{userId}")
    public String showUser(@PathVariable("userId") long id, Model model) {
        User user = usersService.getUserById(id);
        model.addAttribute("user", user);
        return "userInfo";
    }

    @GetMapping(value = "/admin", params = "action=delete")
    public String deleteUser(@RequestParam("userId") long id) {
        usersService.delete(id);
        return "redirect:/users/admin/list";
    }

    @PostMapping(value = "/admin", params = "action=showCreateUserForm")
    public String showCreateUserForm() {
        return "createUserForm";
    }

    @PostMapping(value = "/admin", params = "action=create")
    public String create(@ModelAttribute("newUserData") UserDto userDto, SessionStatus sessionStatus) {
        usersService.save(usersService.createUserFromDto(userDto));
        sessionStatus.setComplete();
        return "redirect:/users/admin/list";
    }

    @GetMapping(value = "/admin", params = "action=showUpdateUserForm")
    public String showUpdateUserForm(@RequestParam("userId") long id, Model model) {
        UserDto userData = new UserDto(usersService.getUserById(id));
        model.addAttribute("userData", userData);
        return "updateUserForm";
    }

    @PostMapping(value = "/admin", params = "action=update")
    public String updateUser(@ModelAttribute("userData") UserDto userDto, SessionStatus sessionStatus) {
        usersService.update(userDto.getId(), usersService.createUserFromDto(userDto));
        sessionStatus.setComplete();
        return "redirect:/users/admin/list";
    }

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