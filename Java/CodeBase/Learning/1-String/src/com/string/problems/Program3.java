package com.string.problems;

import java.util.Arrays;

//WAP to check if two strings are Anagram or not
//e.g heart and earth are Anagram strings
public class Program3 {

	public static void main(String[] args) {
		String str1 = "heart";
		String str2 = "earth";
		
		char[] arr1 = str1.toCharArray();
		char[] arr2 = str2.toCharArray();
		
		Arrays.sort(arr1);
		Arrays.sort(arr2);
		
		if(Arrays.equals(arr1, arr2)) {
			System.out.println("is Anagram");
		} else {
			System.out.println("not Anagram");
		}
	}
}
