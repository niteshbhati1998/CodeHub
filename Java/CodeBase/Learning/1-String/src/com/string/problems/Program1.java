package com.string.problems;

import java.util.Stack;

//WAP to reverse a string (Palindrome)
public class Program1 {

	public static void main(String[] args) {
		String str = "Student";
		
		//loop
		String revStr1 = "";
		for(int i=str.length()-1;i>=0;i--) {
			revStr1 += str.charAt(i);
		}
		System.out.println(revStr1);
		
		//stack
		String revStr2 = "";
		Stack<Character> s = new Stack<>();
		for(int i=0;i<str.length();i++) {
			s.push(str.charAt(i));
		}	
		while(!s.isEmpty()) {
			revStr2 += s.pop();
		}
		System.out.println(revStr2);
		
		//recusion
		String revStr3 = reverseUsingRecursion(str);
		System.out.println(revStr3);
		
	}
	
	static String reverseUsingRecursion(String str) {
		if(str.length()==0) {
			return str;
		}
		return str.charAt(str.length()-1) + reverseUsingRecursion(str.substring(0,str.length()-1));
	}
}
