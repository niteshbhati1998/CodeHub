package com.example.basic;

import java.util.HashMap;
import java.util.Map;

//sort map based on value in descending order
public class Problem9 {
    public static void main(String[] args) {
    	Map<String,Integer> map = new HashMap<>();
    	map.put("A", 10);
    	map.put("B", 50);
    	map.put("C", 70);
    	map.put("D", 30);
    	
    	map.entrySet()
				.stream()
				.sorted((a,b)->b.getValue()-a.getValue())
				.forEach(System.out::println);
    } 
}
