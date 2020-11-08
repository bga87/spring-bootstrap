package com.jm.task.dao;


import com.jm.task.domain.Job;
import com.jm.task.domain.Role;
import com.jm.task.domain.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Repository
public class UserDaoImpl implements UserDao {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public Long save(User user) throws IllegalStateException {
        setJobFromPersistentIfAlreadyExists(user, entityManager);
        setRolesFromPersistentIfAlreadyExist(user, entityManager);

        Long savedUserId;
        if (userHasUniqueLogin(user, entityManager)) {
            if (!userIsAlreadyPersisted(user, entityManager)) {
                entityManager.persist(user);
                savedUserId = user.getId();
            } else {
                throw new IllegalStateException("Пользователь " + user + " был сохранен в базе данных ранее");
            }
        } else {
            throw new IllegalStateException("Логин " + user.getSecurityDetails().getEmail() + " уже занят! Придумайте другой логин.");
        }

        return savedUserId;
    }

    private void setRolesFromPersistentIfAlreadyExist(User user, EntityManager entityManager) {
        Set<Role> correspondingPersistentRoles = user.getSecurityDetails().getRoles().stream()
                .filter(role -> role.getId() != null)
                .map(entityManager::merge)
                .collect(Collectors.toSet());

        if (correspondingPersistentRoles.size() > 0) {
            user.getSecurityDetails().setRoles(correspondingPersistentRoles);
        }
    }

    private boolean userIsAlreadyPersisted(User user, EntityManager entityManager) {
        return entityManager.createQuery("SELECT u FROM User u " +
                "WHERE u.name = :name AND u.surname = :surname AND u.age = :age", User.class)
                .setParameter("name", user.getName())
                .setParameter("surname", user.getSurname())
                .setParameter("age", user.getAge())
                .getResultList()
                .contains(user);
    }

    private boolean userHasUniqueLogin(User user, EntityManager entityManager) {
        return entityManager.createQuery("SELECT u FROM User u " +
                "WHERE u.securityDetails.email = :login", User.class)
                .setParameter("login", user.getSecurityDetails().getEmail())
                .getResultList().size() == 0;
    }

    private void setJobFromPersistentIfAlreadyExists(User user, EntityManager entityManager) {
        user.getJob().flatMap(userJob ->
                entityManager.createQuery("SELECT j FROM Job j " +
                        "WHERE j.name = :name AND j.salary = :salary", Job.class)
                        .setParameter("name", userJob.getName())
                        .setParameter("salary", userJob.getSalary())
                        .getResultStream().findFirst()
        ).ifPresent(user::setJob);
    }

    @Override
    public void delete(long id) {
        User loadedUser = entityManager.find(User.class, id);

        if (loadedUser != null) {
            entityManager.remove(loadedUser);
            loadedUser.getJob().ifPresent(job -> removeIfOrphanJob(job, entityManager));
        }
    }

    private void removeIfOrphanJob(Job job, EntityManager entityManager) {
        List<User> usersWithTheSameJob = entityManager.createQuery("SELECT u FROM User u " +
                "WHERE u.job = :job", User.class)
                .setParameter("job", job)
                .getResultList();

        if (usersWithTheSameJob.size() == 0) {
            // на эту запись job больше никто не ссылается из таблицы user, ее можно удалять
            entityManager.remove(job);
        }
    }

    @Override
    public List<User> listUsers() {
        return entityManager.createQuery("SELECT DISTINCT u FROM User u " +
                "LEFT JOIN FETCH u.securityDetails.roles " +
                "LEFT JOIN FETCH u.job", User.class).getResultList();
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
        return entityManager.createQuery("SELECT u FROM User u LEFT JOIN FETCH u.securityDetails.roles " +
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

        if (!userIsAlreadyPersisted(user, entityManager)) {
            targetUser.setName(user.getName());
            targetUser.setSurname(user.getSurname());
            targetUser.setAge(user.getAge());

            if (!targetUser.getSecurityDetails().equals(user.getSecurityDetails())) {
                // данные доступа поменялись
                if (!loginsAreTheSame(targetUser.getSecurityDetails().getEmail(), user.getSecurityDetails().getEmail()) &&
                        !userHasUniqueLogin(user, entityManager)) {
                    throw new IllegalStateException("Логин " + user.getSecurityDetails().getEmail() + " уже занят! Придумайте другой логин.");
                }
                targetUser.setSecurityDetails(user.getSecurityDetails());
                setRolesFromPersistentIfAlreadyExist(targetUser, entityManager);
            }

            if (!jobsAreTheSame(targetUser.getJob(), user.getJob())) {
                setJobFromPersistentIfAlreadyExists(user, entityManager);
                Optional<Job> oldUserJob = targetUser.getJob();
                targetUser.setJob(user.getJob().orElse(null));
                oldUserJob.ifPresent(oldJob -> removeIfOrphanJob(oldJob, entityManager));
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