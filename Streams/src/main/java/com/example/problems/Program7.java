package com.example.problems;

import java.util.Arrays;
import java.util.List;

//WAP to print distinct elements(without duplicates) from a list
public class Program7 {
    public static void main(String[] args) {
    	
    	List<Integer> list = Arrays.asList(50,70,30,50,10,40,30,50,30);
    	
    	list.stream().distinct().forEach(System.out::println);
    } 
}
