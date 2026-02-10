package com.example.caching.repository;

import com.example.caching.entity.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherRepository extends JpaRepository<Weather, Long> {

    Weather findByCity(String city);

    void deleteByCity(String city);
}
