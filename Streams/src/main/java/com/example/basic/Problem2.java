package com.example.basic;

import java.util.Arrays;
import java.util.List;

//find max element from list
public class Problem2 {
    public static void main(String[] args) {
    	
    	List<Integer> list = Arrays.asList(50,70,30,10,40);
    	
    	//sorting
    	int max = list.stream().sorted((a,b)->b-a).findFirst().get();
    	System.out.println(max);
    	
    	//IntStream
    	int max1 = list.stream().mapToInt(Integer::intValue).max().getAsInt();
    	System.out.println(max1);
    	
    	//reduce
        int max2 = list.stream().reduce(Integer.MIN_VALUE, (a,b)->Integer.max(a, b));
        System.out.println(max2);
	} 
}
