package com.jm.task.domain;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Embeddable
public class SecurityDetails {

    @NotBlank(message = "Email is required")
    @Email(message="Not a valid email address format")
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 3, message = "Password must be at least 3 characters long")
    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public SecurityDetails() {
    }

    public SecurityDetails(String email, String password, Set<Role> roles) {
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String loginName) {
        this.email = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password, roles);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || ((obj instanceof SecurityDetails) &&
                ((SecurityDetails) obj).email.equalsIgnoreCase(email) &&
                ((SecurityDetails) obj).password.equals(password) &&
                ((SecurityDetails) obj).roles.equals(roles));
    }

    @Override
    public String toString() {
        return "SecurityDetails{" +
                "login='" + email + '\'' +
                ", password='" + password + '\'' +
                ", roles=" + roles +
                '}';
    }
}