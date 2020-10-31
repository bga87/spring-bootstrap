package com.jm.task.dao;


import com.jm.task.domain.User;

import java.util.List;


public interface UserDao {
    Long save(User user) throws IllegalStateException;
    void delete(long id);
    List<User> listUsers();
    User getUserById(long id);
    User getUserByLogin(String login);
    void update(long id, User user) throws IllegalStateException;
}