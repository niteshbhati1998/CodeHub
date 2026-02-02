package com.example.jpa.service;

import com.example.jpa.entity.*;
import com.example.jpa.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class MyService {

    @Autowired
    private PassportRepository passportRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

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

    public List<Person> getPerson(String name) {
        return personRepository.findByName(name);
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
