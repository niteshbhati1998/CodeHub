package com.streams.employee;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Problem1 {

    public static void main(String[] args) {
        List<Employee> list = new ArrayList<>();
        list.add(new Employee("A", 50000));
        list.add(new Employee("B", 25000));
        list.add(new Employee("A", 75000));
        list.add(new Employee("C", 35000));
        list.add(new Employee("D", 25000));

        //sort list in decreasing order of salary
        list.stream().sorted((a, b) -> b.getSalary() - a.getSalary()).forEach(System.out::println);

        //filter out employees whose name starts with A
        list.stream().filter(n -> n.getName().startsWith("A")).forEach(System.out::println);

        //find employee having maximum salary
        Optional<Employee> e = list.stream().sorted((a, b) -> b.getSalary() - a.getSalary()).findFirst();
        System.out.println(e.get());

        //find employee having second maximum salary
        Optional<Employee> e1 = list.stream().sorted((a, b) -> b.getSalary() - a.getSalary()).skip(1).findFirst();
        System.out.println(e1.get());

        //generate a map whose key will be salary and value will be count of employees having that salary
        Map<Integer, Long> map = list.stream().collect(Collectors.groupingBy(n -> n.getSalary(), Collectors.counting()));
        System.out.println(map);
    }
}
