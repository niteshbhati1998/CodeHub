package com.example.problems;

import java.util.Arrays;
import java.util.List;

//WAP to find even & odd numbers from a list (TCS)
public class Program4 {
	public static void main(String[] args) {

		List<Integer> list = Arrays.asList(10,15,20,25,30);

		//even
		list.stream().filter(n->n%2==0).forEach(System.out::println);

		//odd
		list.stream().filter(n->n%2!=0).forEach(System.out::println);
	} 
}
