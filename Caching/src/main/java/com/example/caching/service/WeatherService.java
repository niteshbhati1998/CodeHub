package com.example.caching.service;

import com.example.caching.entity.Weather;
import com.example.caching.repository.WeatherRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeatherService {

    @Autowired
    WeatherRepository weatherRepository;

    public Weather saveWeather(Weather weather) {
        return weatherRepository.save(weather);
    }

    public List<Weather> getWeather() {
        return weatherRepository.findAll();
    }

    @Cacheable(value = "weather", key = "#city", unless = "#result == null")
    public Weather getWeatherByCity(String city) {
        return weatherRepository.findByCity(city);
    }

    @CachePut(value = "weather", key = "#city")
    public Weather updateWeatherByCity(String city, String forecast) {
        Weather weather = weatherRepository.findByCity(city);
        weather.setForecast(forecast);
        return weatherRepository.save(weather);
    }

    @Transactional
    @CacheEvict(value = "weather", key = "#city")
    public void deleteByCity(String city) {
        weatherRepository.deleteByCity(city);
    }
}
