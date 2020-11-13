package com.jm.task.dao;


import com.jm.task.domain.Job;
import com.jm.task.domain.Role;
import com.jm.task.domain.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnitUtil;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Repository
public class UserDaoImpl implements UserDao {

    @PersistenceContext
    EntityManager entityManager;

    //Ok
    @Override
    public Long save(User user) throws IllegalStateException {
        // проверяем, что user'а с такими данными в базе нет
        if (isAlreadyInDatabase(user)) {
            throw new IllegalStateException("User " + user + " has already been saved to database");
        }
        // проверяем, что email user'а уникален в базе
        if (!hasUniqueEmail(user)) {
            throw new IllegalStateException(
                    "Email " + user.getSecurityDetails().getEmail() + " belongs to another user; must be unique");
        }
        setJobFromPersistentIfAlreadyExists(user);
        setRolesFromPersistentIfAlreadyExist(user);
        entityManager.persist(user);
        return user.getId();
    }

    //Ok
    private boolean isAlreadyInDatabase(User user) {
        return entityManager.createQuery("SELECT u FROM User u " +
                "LEFT JOIN FETCH u.job " +
                "WHERE u.name = :name AND u.surname = :surname AND u.age = :age", User.class)
                .setParameter("name", user.getName())
                .setParameter("surname", user.getSurname())
                .setParameter("age", user.getAge())
                .getResultList()
                .contains(user);
    }

    //Ok
    private boolean hasUniqueEmail(User user) {
        return entityManager.createQuery("SELECT u FROM User u " +
                "WHERE u.securityDetails.email = :email", User.class)
                .setParameter("email", user.getSecurityDetails().getEmail())
                .getResultList().isEmpty();
    }

    //Ok
    private void setRolesFromPersistentIfAlreadyExist(User user) {
        Set<Role> userRoles = user.getSecurityDetails().getRoles();
        if (!userRoles.isEmpty()) {
            Set<Role> persistedRoles = userRoles.stream()
                    .filter(role -> role.getId() != null)
                    .map(entityManager::merge)
                    .collect(Collectors.toSet());
            Set<Role> transientRoles = userRoles.stream()
                    .filter(role -> role.getId() == null)
                    .collect(Collectors.toSet());
            user.getSecurityDetails().setRoles(
                    Stream.concat(persistedRoles.stream(), transientRoles.stream())
                            .collect(Collectors.toSet())
            );
        }
    }

    //Ок
    private void setJobFromPersistentIfAlreadyExists(User user) {
        user.getJob().flatMap(userJob ->
                entityManager.createQuery("SELECT j FROM Job j " +
                        "WHERE j.name = :name AND j.salary = :salary", Job.class)
                        .setParameter("name", userJob.getName())
                        .setParameter("salary", userJob.getSalary())
                        .getResultStream().findFirst()
        ).ifPresent(user::setJob);
    }

    //Ok
    @Override
    public void delete(long id) {
        User deletionCandidate = entityManager.find(User.class, id);

        if (deletionCandidate != null) {
            entityManager.remove(deletionCandidate);
            deletionCandidate.getJob().ifPresent(this::removeIfOrphanJob);
        }
    }

    //Ok
    private void removeIfOrphanJob(Job job) {
        List usersWithTheSameJob = entityManager.createQuery("SELECT u.id FROM User u " +
                "WHERE u.job = :job")
                .setParameter("job", job)
                .getResultList();

        if (usersWithTheSameJob.isEmpty()) {
            // на эту запись job больше никто не ссылается из таблицы user, ее можно удалять
            entityManager.remove(job);
        }
    }

    @Override
    public List<User> listUsers() {
        return entityManager.createQuery("SELECT DISTINCT u FROM User u " +
                "LEFT JOIN FETCH u.securityDetails.roles " +
                "LEFT JOIN FETCH u.job", User.class)
                .getResultList();
    }

    @Override
    public User getUserById(long id) {
        return entityManager.createQuery("SELECT u FROM User u " +
                "LEFT JOIN FETCH u.securityDetails.roles " +
                "LEFT JOIN FETCH u.job " +
                "WHERE u.id = :id", User.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    @Override
    public User getUserByLogin(String login) {
        return entityManager.createQuery("SELECT u FROM User u " +
                "LEFT JOIN FETCH u.securityDetails.roles " +
                "LEFT JOIN FETCH u.job " +
                "WHERE u.securityDetails.email = :login", User.class)
                .setParameter("login", login)
                .getSingleResult();
    }

    @Override
    public void update(long id, User user) throws IllegalStateException {
        User targetUser = entityManager.find(User.class, id);

        // Если изменненый user идентичен соответствующему ему из БД,
        // то дальнейшее выполнение метода не имеет смысла
        if (targetUser.equals(user)) {
            return;
        }

        if (!isAlreadyInDatabase(user)) {
            targetUser.setName(user.getName());
            targetUser.setSurname(user.getSurname());
            targetUser.setAge(user.getAge());

            if (!targetUser.getSecurityDetails().equals(user.getSecurityDetails())) {
                // данные доступа поменялись
                if (!loginsAreTheSame(targetUser.getSecurityDetails().getEmail(), user.getSecurityDetails().getEmail()) &&
                        !hasUniqueEmail(user)) {
                    throw new IllegalStateException("Логин " + user.getSecurityDetails().getEmail() + " уже занят! Придумайте другой логин.");
                }
                targetUser.setSecurityDetails(user.getSecurityDetails());
                setRolesFromPersistentIfAlreadyExist(targetUser);
            }

            if (!jobsAreTheSame(targetUser.getJob(), user.getJob())) {
                setJobFromPersistentIfAlreadyExists(user);
                Optional<Job> oldUserJob = targetUser.getJob();
                targetUser.setJob(user.getJob().orElse(null));
                oldUserJob.ifPresent(oldJob -> removeIfOrphanJob(oldJob));
            }
        } else {
            throw new IllegalStateException("Пользователь " + user + " был сохранен в базе данных ранее");
        }
    }

    private boolean loginsAreTheSame(String originalLogin, String newLogin) {
        return originalLogin.equals(newLogin);
    }

    private boolean jobsAreTheSame(Optional<Job> originalJobOpt, Optional<Job> newJobOpt) {
        return Objects.equals(originalJobOpt.orElse(null), newJobOpt.orElse(null));
    }
}