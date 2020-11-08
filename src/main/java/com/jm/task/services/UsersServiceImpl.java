package com.jm.task.services;


import com.jm.task.dao.UserDao;
import com.jm.task.domain.Job;
import com.jm.task.domain.Role;
import com.jm.task.domain.SecurityDetails;
import com.jm.task.domain.User;
import com.jm.task.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class UsersServiceImpl implements UsersService {

    private final UserDao userDao;
    private final Set<Role> availableRoles;

    public UsersServiceImpl(UserDao userDao, Set<Role> availableRoles) {
        this.userDao = userDao;
        this.availableRoles = availableRoles;
    }

    @Transactional
    @Override
    public Long save(User u) throws IllegalStateException {
        return userDao.save(u);
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

    @Override
    public User createUserFromDto(UserDto userDto) {
        Job job = !userDto.getJobName().isEmpty() ? new Job(userDto.getJobName(), userDto.getSalary()) : null;
        Set<Role> userRoles = availableRoles.stream()
                .filter(role -> userDto.getRoles().contains(role.getRoleName()))
                .collect(Collectors.toSet());
        return new User(userDto.getName(), userDto.getSurname(), userDto.getAge(), job,
                new SecurityDetails(userDto.getEmail(), userDto.getPassword(), userRoles));

    }

}