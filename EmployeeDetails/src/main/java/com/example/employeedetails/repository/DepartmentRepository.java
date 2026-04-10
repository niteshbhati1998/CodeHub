package com.example.employeedetails.repository;

import com.example.employeedetails.entity.Department;
import com.example.employeedetails.model.DepartmentDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByDeptName(String name);

//    @Query("select d from Department d JOIN d.employeeList e where e.id= :empId")
//    Optional<Department> findByEmployeeId(@Param("empId") Long id);
//
//    @Query("select d from Department d JOIN d.employeeList e where e.empName= :empName")
//    Optional<Department> findByEmployeeName(@Param("empName") String name);

    @Query("select new DepartmentDto(d.id, d.deptName, e.id, e.empName) from Department d JOIN d.employeeList e where e.id= :empId")
    Optional<DepartmentDTO> findByEmployeeId(@Param("empId") Long id);

    @Query("select new DepartmentDto(d.id, d.deptName, e.id, e.empName) from Department d JOIN d.employeeList e where e.empName= :empName")
    Optional<DepartmentDTO> findByEmployeeName(@Param("empName") String name);
}
