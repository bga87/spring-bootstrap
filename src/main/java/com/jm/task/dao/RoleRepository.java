package com.jm.task.dao;


import com.jm.task.domain.Role;
import org.springframework.data.repository.Repository;

import java.util.Optional;


public interface RoleRepository extends Repository<Role, Long> {

    Optional<Role> findById(Long id);
    Role save(Role role);

}