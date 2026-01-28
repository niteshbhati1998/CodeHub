package com.example.problems;

import java.util.Arrays;
import java.util.List;

//WAP to find max element from a list
public class Program2 {
    public static void main(String[] args) {
    	
    	List<Integer> list = Arrays.asList(50,70,30,10,40);
    	
    	//sorting
    	int max1 = list.stream().sorted((a,b)->b-a).findFirst().get();
    	System.out.println(max1);
    	
    	//mapToInt
    	int max2 = list.stream().mapToInt(Integer::intValue).max().getAsInt();
    	System.out.println(max2);
    	
    	//reduce
        int max3 = list.stream().reduce(Integer.MIN_VALUE, (a,b)->Integer.max(a, b));
        System.out.println(max3);
	} 
}
