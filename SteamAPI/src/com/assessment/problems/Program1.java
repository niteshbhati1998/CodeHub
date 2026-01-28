package com.assessment.problems;

import java.util.Arrays;
import java.util.List;

//WAP to find min element from a list
public class Program1 {
    public static void main(String[] args) {
    	
    	List<Integer> list = Arrays.asList(50,70,30,10,40);
    	
    	//sorting
    	int min1 = list.stream().sorted().findFirst().get();
    	System.out.println(min1);
    	
    	//mapToInt
    	int min2 = list.stream().mapToInt(Integer::intValue).min().getAsInt();
    	System.out.println(min2);
    	
    	//reduce
        int min3 = list.stream().reduce(Integer.MAX_VALUE, (a,b)->Integer.min(a, b));
        System.out.println(min3); 
	} 
}
