package com.assessment.problems;

import java.util.Arrays;
import java.util.List;

//WAP to find second largest element from a list
public class Program3 {
    public static void main(String[] args) {
    	
    	List<Integer> list = Arrays.asList(50,70,30,10,40);
    	
    	int val = list.stream().sorted((a,b)->b-a).skip(1).findFirst().get();
    	System.out.println(val);
    } 
}
