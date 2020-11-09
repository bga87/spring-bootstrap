package com.jm.task.services;


import com.jm.task.dao.UserDao;
import com.jm.task.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;


@Service
public class UsersServiceImpl implements UsersService {

    private final UserDao userDao;

    public UsersServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Transactional
    @Override
    public Long save(User user) throws IllegalStateException {
        setJobToNullIfJobNameIsEmpty(user);
        return userDao.save(user);
    }

    @Transactional
    @Override
    public void delete(long id) {
        userDao.delete(id);
    }

    @Transactional
    @Override
    public List<User> listUsers() {
        return userDao.listUsers();
    }

    @Transactional
    @Override
    public User getUserById(long id) {
        return userDao.getUserById(id);
    }

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return userDao.getUserByLogin(s);
    }

    @Transactional
    @Override
    public void update(long id, User user) throws IllegalStateException {
        userDao.update(id, user);
    }

    private void setJobToNullIfJobNameIsEmpty(User user) {
        user.getJob().ifPresent(job -> {
            if (job.getName().isEmpty()) {
                user.setJob(null);
            }
        });
    }
}