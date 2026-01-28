package com.example.problems;

import java.util.HashMap;
import java.util.Map;

//Sort employees based on their salary in descending order from given map (TCS)
public class Program93 {
    public static void main(String[] args) {
    	Map<String,Integer> map = new HashMap<>();
    	map.put("A", 10);
    	map.put("B", 50);
    	map.put("C", 70);
    	map.put("D", 30);
    	
    	map.entrySet().stream().sorted((a,b)->b.getValue()-a.getValue()).forEach(System.out::println);
    } 
}
