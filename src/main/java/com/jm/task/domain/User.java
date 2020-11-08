package com.jm.task.domain;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"name", "surname", "age", "job_id"}
                ),
        @UniqueConstraint(
                columnNames = {"email"}
                )
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(generator = "ID_GENERATOR")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private byte age;

    private SecurityDetails securityDetails;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn
    private Job job;

    public User() {}

    public User(String name, String surname, byte age, Job job, SecurityDetails securityDetails) {
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.job = job;
        this.securityDetails = securityDetails;
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

    public SecurityDetails getSecurityDetails() {
        return securityDetails;
    }

    public void setSecurityDetails(SecurityDetails securityDetails) {
        this.securityDetails = securityDetails;
    }

    public Optional<Job> getJob() {
        return Optional.ofNullable(job);
    }

    public void setJob(Job job) {
        this.job = job;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof User &&
                ((User) obj).name.equalsIgnoreCase(name) &&
                ((User) obj).surname.equalsIgnoreCase(surname) &&
                ((User) obj).age == age &&
                Objects.equals(job, ((User) obj).job) &&
                Objects.equals(securityDetails, ((User) obj).securityDetails));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, surname, age, job);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return securityDetails.getRoles();
    }

    @Override
    public String getPassword() {
        return securityDetails.getPassword();
    }

    @Override
    public String getUsername() {
        return securityDetails.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", age=" + age +
                ", job=" + job +
                ", securityDetails=" + securityDetails +
                '}';
    }
}