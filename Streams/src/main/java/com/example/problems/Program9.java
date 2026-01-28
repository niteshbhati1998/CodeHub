package com.example.problems;

import java.util.stream.IntStream;

//WAP to check String is Palindrome or not (TCS)
public class Program9 {
    public static void main(String[] args) {
    	
    	String str = "madam";  
    	
    	//two-pointer technique
    	boolean b = IntStream.range(0, str.length()/2).allMatch(n->str.charAt(n)==str.charAt(str.length()-1-n));
    	if(b) {
    		System.out.println("Palindrome");
    	} else {
    		System.out.println("Not Palindrome");
    	}
    } 
}
