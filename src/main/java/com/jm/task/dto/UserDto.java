package com.jm.task.dto;


import com.jm.task.domain.Job;
import com.jm.task.domain.Role;
import com.jm.task.domain.User;

import java.util.Set;
import java.util.stream.Collectors;


public class UserDto {

    private Long id;
    private String name;
    private String surname;
    private byte age;
    private String jobName;
    private int salary;
    private  String login;
    private String password;
    private Set<String> roles;

    public UserDto() {
        name = "Введите имя";
        surname = "Ведите фамилию";
        jobName = "Введите профессию";
        login = "Введите логин";
    }

    public UserDto(User user) {
        id = user.getId();
        name = user.getName();
        surname = user.getSurname();
        age = user.getAge();
        login = user.getSecurityDetails().getLogin();
        password = user.getSecurityDetails().getPassword();
        roles = user.getSecurityDetails().getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());
        jobName = user.getJob().map(Job::getName).orElse("");
        salary = user.getJob().map(Job::getSalary).orElse(0);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public byte getAge() {
        return age;
    }

    public void setAge(byte age) {
        this.age = age;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", age=" + age +
                ", jobName='" + jobName + '\'' +
                ", salary=" + salary +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", roles=" +  roles +
                '}';
    }
}