package com.example.caching.controller;

import com.example.caching.entity.Weather;
import com.example.caching.model.Response;
import com.example.caching.service.CacheInspectionService;
import com.example.caching.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private CacheInspectionService cacheInspectionService;

    @PostMapping("/save")
    public ResponseEntity<Response> saveWeather(@RequestBody Weather weather) {
        Response<Weather> response = new Response<>();
        try {
            Weather savedWeather = weatherService.saveWeather(weather);
            response.setMessage("Weather saved successfully");
            response.setPayload(savedWeather);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get")
    public ResponseEntity<Response> getWeather() {
        Response<List<Weather>> response = new Response<>();
        try {
            List<Weather> list = weatherService.getWeather();
            response.setMessage("Weather fetched successfully");
            response.setPayload(list);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get/{city}")
    public ResponseEntity<Response> getWeatherByCity(@PathVariable String city) {
        Response<Weather> response = new Response<>();
        try {
            Weather weather = weatherService.getWeatherByCity(city);
            response.setMessage("Weather fetched successfully");
            response.setPayload(weather);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<Response> updateWeatherByCity(@RequestParam String city, @RequestParam String forecast) {
        Response<Weather> response = new Response<>();
        try {
            Weather weather = weatherService.updateWeatherByCity(city, forecast);
            response.setMessage("Weather updated successfully");
            response.setPayload(weather);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/cache/inspection")
    public ResponseEntity<Response> getCacheContents() {
        Response<List<Weather>> response = new Response<>();
        try {
            cacheInspectionService.checkCacheContents();
            response.setMessage("Checking cache contents");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
