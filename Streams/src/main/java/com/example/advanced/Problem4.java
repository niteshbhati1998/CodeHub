package com.example.advanced;

import java.util.stream.IntStream;

//check string is palindrome or not
public class Problem4 {
    public static void main(String[] args) {
    	
    	String str = "madam";  
    	
    	//two-pointer technique
    	boolean b = IntStream.range(0, str.length()/2).allMatch(n->str.charAt(n)==str.charAt(str.length()-1-n));
    	if(b) {
    		System.out.println("palindrome");
    	} else {
    		System.out.println("not palindrome");
    	}
    } 
}
