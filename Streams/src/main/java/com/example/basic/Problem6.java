package com.example.basic;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

//find duplicate elements from list
public class Problem6 {
    public static void main(String[] args) {
    	
    	List<Integer> list = Arrays.asList(50,70,30,50,10,40,30,50,30);
    	
        //hashset
    	Set<Integer> hs = new HashSet<>();
    	list.stream().filter(n->!hs.add(n)).distinct().forEach(System.out::println);
    	
    	//collections
    	list.stream().filter(n->Collections.frequency(list, n)>1).distinct().forEach(System.out::println);
    	
    	//map
    	list.stream()
    		.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    		.entrySet()
    		.stream()
    		.filter(n->n.getValue()>1)
    		.forEach(n->System.out.println(n.getKey()));   
    	
    	Map<Integer, Long> map = list.stream()		
		    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
		    .entrySet()
		    .stream()
		    .filter(n->n.getValue()>1)
		    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    	System.out.println(map);
    } 
}
