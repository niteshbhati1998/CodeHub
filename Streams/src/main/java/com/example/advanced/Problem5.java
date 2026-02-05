package com.example.advanced;

import java.util.Arrays;
import java.util.List;

//count number of hello in list
public class Problem5 {
    public static void main(String[] args) {
    	
    	List<String> list = Arrays.asList("hello world    hello", "world of Java",
				                          "hello Java", "hello world of    Java");
		
    	long count = list.stream()
				.flatMap(n->Arrays.stream(n.replaceAll(" +", " ").split(" ")))
				.filter(n->n.equals("hello"))
				.count();
    	System.out.println(count);
    } 
}
