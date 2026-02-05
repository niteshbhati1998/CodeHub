package com.example.basic;

import java.util.Arrays;
import java.util.List;

//find min element from list
public class Problem1 {
    public static void main(String[] args) {
    	
    	List<Integer> list = Arrays.asList(50,70,30,10,40);
    	
    	//sorting
    	int min = list.stream().sorted().findFirst().get();
    	System.out.println(min);

    	//IntStream
    	int min1 = list.stream().mapToInt(Integer::intValue).min().getAsInt();
    	System.out.println(min1);
    	
    	//reduce
        int min2 = list.stream().reduce(Integer.MAX_VALUE, (a,b)->Integer.min(a, b));
        System.out.println(min2);
	} 
}
