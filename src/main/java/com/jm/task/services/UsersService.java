package com.jm.task.services;


import com.jm.task.domain.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;


public interface UsersService extends UserDetailsService {
    Long save(User user) throws IllegalStateException;
    void delete(long id);
    List<User> listUsers();
    User getUserById(long id);
    void update(long id, User user) throws IllegalStateException;
}