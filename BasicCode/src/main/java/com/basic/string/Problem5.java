package com.basic.string;

//string palindrome or not
public class Problem5 {

    public static void main(String[] args) {
        String str = "madam";

        //string builder
        String rev = new StringBuilder(str).reverse().toString();
        if(str.equals(rev)) {
            System.out.println("palindrome");
        }
    }
}