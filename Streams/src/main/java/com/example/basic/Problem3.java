package com.example.basic;

import java.util.Arrays;
import java.util.List;

//find second-largest element from list
public class Problem3 {
    public static void main(String[] args) {
    	
    	List<Integer> list = Arrays.asList(50,70,30,10,40);
    	
    	int val = list.stream().sorted((a,b)->b-a).skip(1).findFirst().get();
    	System.out.println(val);
    } 
}
