package com.jm.task;


import com.jm.task.domain.Role;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


@SpringBootApplication
public class SpringBootCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootCrudApplication.class, args);
    }

    @Bean("availableRoles")
    public Set<Role> availableRoles(ServletContext context) {
        Set<Role> roles = new HashSet<>(
                Arrays.asList(
                        new Role("ROLE_ADMIN", "Admin"),
                        new Role("ROLE_USER", "User")
                )
        );
        context.setAttribute("availableRoles", roles);
        return roles;
    }
}