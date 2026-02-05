package com.example.advanced;

import java.util.stream.IntStream;

//check no is prime or not
//1 is not a prime no
public class Problem3 {

	public static void main(String[] args) {

		int num = 11;
		boolean b = IntStream.rangeClosed(2, num/2).allMatch(n->num%n!=0);
		if (b) {
			System.out.println("prime");
		} else {
			System.out.println("not prime");
		}
	}
}
