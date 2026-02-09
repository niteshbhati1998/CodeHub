package com.example.caching.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "weather", schema = "staging")
@Data
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String city;
    private String forecast;
}
