package com.streams.employee;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//sort list in decreasing order of salary
//filter out employees whose name starts with A
//generate a map whose key will be salary and value will be count of employees having that salary
public class Problem1 {

    public static void main(String[] args) {
        List<Employee> list = new ArrayList<>();
        list.add(new Employee("A", 50000));
        list.add(new Employee("B", 25000));
        list.add(new Employee("A", 75000));
        list.add(new Employee("C", 35000));
        list.add(new Employee("D", 25000));

        list.stream().sorted((a, b) -> b.getSalary() - a.getSalary()).forEach(System.out::println);

        list.stream().filter(n -> n.getName().startsWith("A")).forEach(System.out::println);

        Map<Integer, Long> map = list.stream().collect(Collectors.groupingBy(n -> n.getSalary(), Collectors.counting()));
        System.out.println(map);
    }
}
