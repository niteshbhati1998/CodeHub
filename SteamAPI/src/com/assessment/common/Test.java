package com.assessment.common;

import java.util.ArrayList;
import java.util.List;

//(Deloitte)
public class Test {
    public static void main(String[] args) {
    	
    	List<Employee> list = new ArrayList<>();
    	list.add(new Employee("a",10));
    	list.add(new Employee("b",20));
    	list.add(new Employee("c",30));
    	list.add(new Employee("d",40));
    	
    	//sort Employees based on their salary in descending order
    	list.stream().sorted((a,b)->b.getSalary()-a.getSalary()).forEach(System.out::println);

    	//sort Employees based on their name in descending order
    	list.stream().sorted((a,b)->b.getName().compareTo(a.getName())).forEach(System.out::println);
    	
    	//sort Employees based on their salary first in ascending order then based on their name in descending order
    	list.stream().sorted((a,b)->a.getSalary()-b.getSalary()).sorted((a,b)->b.getName().compareTo(a.getName())).forEach(System.out::println);
    
    	//find Employee having maximum salary
    	Employee e1 = list.stream().sorted((a,b)->b.getSalary()-a.getSalary()).findFirst().get();
    	System.out.println(e1);
    	
    	//find Employee having second maximum salary
    	Employee e2 = list.stream().sorted((a,b)->b.getSalary()-a.getSalary()).skip(1).findFirst().get();
    	System.out.println(e2);
    } 
}
