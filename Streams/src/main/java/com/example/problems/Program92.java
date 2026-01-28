package com.example.problems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//Merge two sorted linked lists into one sorted list (Wipro)
//list1: 1,3,5 list2: 2,4,6  finalList: 1,2,3,4,5,6 
//Note: addAll method doesn't work if we create array using Arrays.asList() directly
public class Program92 {
    public static void main(String[] args) {
    	List<Integer> list1 = new ArrayList<>(Arrays.asList(1,3,5));
    	List<Integer> list2 = new ArrayList<>(Arrays.asList(2,4,6));
    	list1.addAll(list2);                                        
    	
    	List<Integer> list3 = list1.stream().sorted().collect(Collectors.toList());
    	System.out.println(list3);
    } 
}
