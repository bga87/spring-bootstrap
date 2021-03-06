package com.jm.task.dao;


import com.jm.task.domain.Job;
import com.jm.task.domain.Role;
import com.jm.task.domain.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Repository
public class UserDaoImpl implements UserDao {

    private final UserRepository userRepo;
    private final JobRepository jobRepo;
    private final RoleRepository roleRepo;

    public UserDaoImpl(UserRepository userRepo, JobRepository jobRepo, RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.jobRepo = jobRepo;
        this.roleRepo = roleRepo;
    }

    @Override
    public User save(User user) throws IllegalStateException {
        setJobToNullIfJobNameIsEmpty(user);
        // проверяем, что user'а с такими данными в базе нет
        if (isAlreadyInDatabase(user)) {
            throw new IllegalStateException("User " + user + " has already been saved to database before");
        }
        // проверяем, что email user'а уникален в базе
        if (!hasUniqueEmail(user)) {
            throw new IllegalStateException("Email " + user.getEmail() + " belongs to another user; must be unique");
        }
        setJobFromPersistentIfExists(user);
        mergeDetachedRoles(user);
        return userRepo.save(user);
    }

    @Override
    public void delete(long id) {
        userRepo.findWithFetchedJobById(id).ifPresent(user -> {
            Optional<Job> droppedUserJob = user.getJob();
            userRepo.delete(user);
            droppedUserJob.ifPresent(this::deleteIfOrphanedJob);
        });
    }

    @Override
    public List<User> listUsers() {
        return userRepo.findAll();
    }

    @Override
    public User getUserByLogin(String email) {
        return userRepo.findWithFetchedJobAndRolesByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with login/email '" + email + "' not found"));
    }

    @Override
    public void update(Long idToUpdate, User modifiedUser) throws IllegalStateException {
        User userToUpdate = userRepo.findById(idToUpdate)
                .orElseThrow(() -> new RuntimeException("User with id '" + idToUpdate + "' doesn't exist"));
        setJobToNullIfJobNameIsEmpty(modifiedUser);
        // Если изменненый user идентичен соответствующему ему из БД,
        // то дальнейшее выполнение метода не имеет смысла
        if (userToUpdate.equals(modifiedUser)) {
            return;
        }
        if (!isUniqueAfterUpdate(idToUpdate, modifiedUser)) {
            throw new IllegalStateException("Modified user " + modifiedUser + " will duplicate user already in database; duplicates are not allowed");
        }
        if (emailsAreDifferent(userToUpdate, modifiedUser) && !hasUniqueEmail(modifiedUser)) {
            throw new IllegalStateException("Email " + modifiedUser.getEmail() + " belongs to another user; must be unique");
        }
        userToUpdate.setName(modifiedUser.getName());
        userToUpdate.setSurname(modifiedUser.getSurname());
        userToUpdate.setAge(modifiedUser.getAge());
        userToUpdate.setEmail(modifiedUser.getEmail());
        userToUpdate.setPassword(modifiedUser.getPassword());
        userToUpdate.setRoles(modifiedUser.getRoles());
        mergeDetachedRoles(userToUpdate);

        if (jobsAreDifferent(userToUpdate, modifiedUser)) {
            // данные о работе поменялись
            Optional<Job> oldUserJob = userToUpdate.getJob();
            userToUpdate.setJob(modifiedUser.getJob().orElse(null));
            setJobFromPersistentIfExists(userToUpdate);
            oldUserJob.ifPresent(this::deleteIfOrphanedJob);
        }
    }

    private boolean isUniqueAfterUpdate(Long idToUpdate, User modifiedUser) {
        return userRepo.findAllWithJobsByNameAndSurnameAndAgeAndIdNotAllIgnoreCase(
                modifiedUser.getName(), modifiedUser.getSurname(), modifiedUser.getAge(), idToUpdate)
                .stream()
                .noneMatch(selectedUser -> selectedUser.getJob().equals(modifiedUser.getJob()));
    }

    private boolean isAlreadyInDatabase(User user) {
        return userRepo.findAllWithJobsByNameAndSurnameAndAgeAllIgnoreCase(
                user.getName(), user.getSurname(), user.getAge())
                .stream()
                .anyMatch(selectedUser -> selectedUser.getJob().equals(user.getJob()));
    }

    private boolean hasUniqueEmail(User user) {
        return userRepo.findAllByEmail(user.getEmail()).isEmpty();
    }

    private void mergeDetachedRoles(User user) {
        Set<Role> persistentRoles = user.getRoles().stream()
                .map(Role::getId)
                .map(roleRepo::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
        user.setRoles(persistentRoles);
    }

    private void setJobFromPersistentIfExists(User user) {
        user.getJob().flatMap(userJob ->
                jobRepo.findByNameAndSalary(userJob.getName(), userJob.getSalary()))
                .ifPresent(user::setJob);
    }

    private void deleteIfOrphanedJob(Job job) {
        if (userRepo.findAllByJob(job).isEmpty()) {
            jobRepo.delete(job);
        }
    }

    private void setJobToNullIfJobNameIsEmpty(User user) {
        user.getJob().ifPresent(job -> {
            if (job.getName().isEmpty()) {
                user.setJob(null);
            }
        });
    }

    private boolean emailsAreDifferent(User userToUpdate, User modifiedUser) {
        return !userToUpdate.getEmail().equals(modifiedUser.getEmail());
    }

    private boolean jobsAreDifferent(User userToUpdate, User modifiedUser) {
        return !userToUpdate.getJob().equals(modifiedUser.getJob());
    }

}