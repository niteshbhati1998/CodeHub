package com.string.problems;

//WAP to reverse a string using two pointer technique
public class Program2 {

	public static void main(String[] args) {
		String str = "Student";
		
		char[] arr = str.toCharArray();
		
		int start = 0;
		int end = str.length()-1;
		while(start<end) {
			char temp = arr[start];
			arr[start] = arr[end];
			arr[end] = temp;
			
			start++;
			end--;		
		}
		System.out.println(new String(arr));
	}
}
