package com.streams.list.advanced;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

//find prime no's from list
//1 is not a prime no
public class Problem2 {

	public static void main(String[] args) {

		List<Integer> list = Arrays.asList(2, 5, 6, 7, 11, 13, 15);
		list.stream()
				.filter(n -> IntStream.rangeClosed(2, n/2).allMatch(i->n%i!=0))
				.forEach(System.out::println);
	}
}
