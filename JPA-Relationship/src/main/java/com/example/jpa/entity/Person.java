package com.example.jpa.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "person", schema = "staging")
@Data
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;
    private int age;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "passport_id", nullable = false)
    private Passport passport;
}