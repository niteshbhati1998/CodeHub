package com.example.jpa.controller;

import com.example.jpa.entity.Department;
import com.example.jpa.entity.Employee;
import com.example.jpa.entity.Passport;
import com.example.jpa.entity.Person;
import com.example.jpa.model.Response;
import com.example.jpa.service.MyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MyController {

    @Autowired
    private MyService myService;

    @PostMapping("/save/passport")
    public ResponseEntity<Response> savePassport(@RequestBody Passport passport) {
        Response response = new Response();
        try {
            Passport savedPassport = myService.savePassport(passport);
            response.setMessage("success");
            response.setPayload(savedPassport);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/save/person")
    public ResponseEntity<Response> savePerson(@RequestBody Person person) {
        Response response = new Response();
        try {
            Person savedPerson = myService.savePerson(person);
            response.setMessage("success");
            response.setPayload(savedPerson);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/save/person/passport")
    public ResponseEntity<Response> savePersonPassport(@RequestBody Person person) {
        Response response = new Response();
        try {
            Person savedPersonPassport = myService.savePersonPassport(person);
            response.setMessage("success");
            response.setPayload(savedPersonPassport);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get/person/{name}")
    public ResponseEntity<Response> getPerson(@PathVariable String name) {
        Response response = new Response();
        try {
            List<Person> person = myService.getPerson(name);
            response.setMessage("success");
            response.setPayload(person);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get/passport/{id}")
    public ResponseEntity<Response> getPassport(@PathVariable Long id) {
        Response response = new Response();
        try {
            Passport passport = myService.getPassport(id);
            response.setMessage("success");
            response.setPayload(passport);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/save/department/employee")
    public ResponseEntity<Response> saveDepartment(@RequestBody Department department) {
        Response response = new Response();
        try {
            Department savedDepartment = myService.saveDepartment(department);
            response.setMessage("success");
            response.setPayload(savedDepartment);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            response.setMessage("failure: " + ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
