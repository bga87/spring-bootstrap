package com.jm.task.domain;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.AssertTrue;
import java.util.Objects;


@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "salary"}))
public class Job {

    @Id
    @GeneratedValue(generator = "ID_GENERATOR")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name = "";

    @Column(nullable = false)
    private Integer salary;

    public Job() {
    }

    public Job(String name, Integer salary) {
        this.name = name;
        this.salary = salary;
    }

    @AssertTrue(message="Salary is required for employed user")
    public boolean isSalaryValid() {
        return name.isEmpty() || salary != null;
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

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || ((obj instanceof Job) &&
                ((Job) obj).getName().equalsIgnoreCase(getName()) &&
                ((Job) obj).getSalary().equals(getSalary()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getSalary());
    }

    @Override
    public String toString() {
        return "[name: '" + name + '\'' +
                ", salary: " + salary +
                ']';
    }

}