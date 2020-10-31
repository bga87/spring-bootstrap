package com.jm.task.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Objects;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "salary"}))
public class Job {

    @Id
    @GeneratedValue(generator = "ID_GENERATOR")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int salary;

    public Job() {}

    public Job(String name, int salary) {
        this.name = name;
        this.salary = salary;
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

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || ((obj instanceof Job) &&
                ((Job) obj).getName().equalsIgnoreCase(getName()) &&
                ((Job) obj).getSalary() == getSalary());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getSalary());
    }

    @Override
    public String toString() {
        return "Job{" +
                "name='" + name + '\'' +
                ", salary=" + salary +
                '}';
    }
}