package com.example.employeedetails.service;

import com.example.employeedetails.entity.Department;
import com.example.employeedetails.entity.Employee;
import com.example.employeedetails.model.DepartmentDTO;
import com.example.employeedetails.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeDetailsService {

    @Autowired
    DepartmentRepository departmentRepository;

    public DepartmentDTO getDepartmentInfo(Department department) {
        Department dept = null;
        if (department.getId() != null) {
            dept = departmentRepository.findById(department.getId()).orElse(null);
        } else if (department.getDeptName() != null) {
            dept = departmentRepository.findByDeptName(department.getDeptName()).orElse(null);
        } else if (department.getEmployeeList() != null) {
            for (Employee e : department.getEmployeeList()) {
                if (e.getId() != null) {
                    return departmentRepository.findByEmployeeId(e.getId()).orElse(null);
//                    String empName = dept.getEmployeeList().stream()
//                            .filter(n -> n.getId().equals(e.getId()))
//                            .map(n -> n.getEmpName())
//                            .findFirst()
//                            .orElse(null);
//                    List<Employee> listt = new ArrayList<>();
//                    listt.add(new Employee(e.getId(), empName));
//                    dept.setEmployeeList(listt);
                } else {
                    return departmentRepository.findByEmployeeName(e.getEmpName()).orElse(null);
//                    Long empId = dept.getEmployeeList().stream()
//                            .filter(n -> n.getEmpName().equals(e.getEmpName()))
//                            .map(n -> n.getId())
//                            .findFirst()
//                            .orElse(0L);
//                    List<Employee> listt = new ArrayList<>();
//                    listt.add(new Employee(empId, e.getEmpName()));
//                    dept.setEmployeeList(listt);
                }
            }
        }

        if (dept != null) {
            return DepartmentDTO.builder()
                    .id(dept.getId())
                    .deptName(dept.getDeptName())
                    .employeeList(dept.getEmployeeList())
                    .build();
        } else {
            return null;
        }

    }
}