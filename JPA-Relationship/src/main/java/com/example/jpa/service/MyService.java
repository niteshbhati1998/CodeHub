package com.example.jpa.service;

import com.example.jpa.entity.m2m.Course;
import com.example.jpa.entity.m2m.Student;
import com.example.jpa.entity.o2m.Department;
import com.example.jpa.entity.o2m.Employee;
import com.example.jpa.entity.o2o.*;
import com.example.jpa.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MyService {

    @Autowired
    private PassportRepository passportRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    public Passport savePassport(Passport passport) {
        return passportRepository.save(passport);
    }

    public Person savePerson(Person person) {
        Optional<Passport> passport = passportRepository.findById(person.getPassport().getId());
        passport.ifPresent(pass-> person.setPassport(pass));
        return personRepository.save(person);
    }

    public Person savePersonPassport(Person person) {
        return personRepository.save(person);
    }

    public Person getPerson(Long id) {
        Optional<Person> person = personRepository.findById(id);
        return person.orElseThrow(()-> new RuntimeException("Person not found"));
    }

    public Passport getPassport(Long id) {
        Optional<Passport> passport = passportRepository.findById(id);
        return passport.orElseThrow(()-> new RuntimeException("Passport not found"));
    }

    public Department saveDepartment(Department department) {
        //set back-reference
        for(Employee e: department.getEmployee()) {
            e.setDepartment(department);
        }
        return departmentRepository.save(department);
    }

    public Student saveStudent(Student student) {
        //set back-reference
        for (Course course : student.getCourses()) {
            course.getStudents().add(student);
        }
        return studentRepository.save(student);
    }
}
