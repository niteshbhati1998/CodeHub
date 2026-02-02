package com.example.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "passport", schema = "staging")
@Data
public class Passport {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String country;
    private String passportNumber;

    @OneToOne(mappedBy = "passport")
    @JsonIgnore
    private Person person;
}