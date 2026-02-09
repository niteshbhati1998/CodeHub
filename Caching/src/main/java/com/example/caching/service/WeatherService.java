package com.example.caching.service;

import com.example.caching.entity.Weather;
import com.example.caching.repository.WeatherRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Cacheable("weather")
    public Weather getWeatherByCity(String city) {
        return weatherRepository.findByCity(city);
    }

    @CachePut("weather")
    public Weather updateWeatherByCity(String city, String forecast) {
        Weather weather = weatherRepository.findByCity(city);
        weather.setForecast(forecast);
        return weatherRepository.save(weather);
    }
}
