package com.streams.employee;

import java.util.ArrayList;
import java.util.List;

//sort list in increasing order of salary, then decreasing order of name
//find employee having maximum salary
//find employee having second maximum salary
public class Problem2 {
    public static void main(String[] args) {

        List<Employee> list = new ArrayList<>();
        list.add(new Employee("A", 50000));
        list.add(new Employee("B", 25000));
        list.add(new Employee("A", 75000));
        list.add(new Employee("C", 35000));
        list.add(new Employee("D", 25000));

        list.stream().sorted((a, b) -> a.getSalary() - b.getSalary())
                .sorted((a, b) -> b.getName().compareTo(a.getName()))
                .forEach(System.out::println);

        Employee e = list.stream().sorted((a, b) -> b.getSalary() - a.getSalary()).findFirst().get();
        System.out.println(e);

        Employee e1 = list.stream().sorted((a, b) -> b.getSalary() - a.getSalary()).skip(1).findFirst().get();
        System.out.println(e1);
    }
}
