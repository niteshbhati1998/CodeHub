package com.example.problems;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Program8 {

	public static void main(String[] args) {
		
		//given no is prime or not
		int num = 11;
		boolean b = IntStream.rangeClosed(2, num/2).allMatch(n->num%n!=0);
		if (b) {
			System.out.println("is prime");
		} else {
			System.out.println("not prime");
		}
		
		//print all prime no's from given list
		List<Integer> list = Arrays.asList(2, 5, 7, 9, 11, 13, 15);
		list.stream().filter(n->n>1)
				     .filter(n->IntStream.rangeClosed(2, n/2).allMatch(i->n%i!=0))
				     .forEach(System.out::println);
		 
		//prime no's between 1 to 100
		Stream.iterate(1, n->n+1).limit(100)
		      .filter(n->n>1)
	          .filter(n->IntStream.rangeClosed(2, n/2).allMatch(i->n%i!=0))
	          .forEach(System.out::println);
	}
}
