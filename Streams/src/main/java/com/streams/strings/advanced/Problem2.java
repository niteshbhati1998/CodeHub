package com.streams.strings.advanced;

import java.util.stream.IntStream;

//string palindrome or not (string == reverse string)
public class Problem2 {
    public static void main(String[] args) {

        String str = "madam";

        //two-pointer technique
        boolean val = IntStream.range(0, str.length() / 2)
                .allMatch(n -> str.charAt(n) == str.charAt(str.length() - 1 - n));
        if (val) {
            System.out.println("palindrome");
        }

        boolean val1 = str.chars()
                .mapToObj(n -> String.valueOf((char) n))
                .reduce("", (a, b) -> b + a)
                .equals(str);
        if (val1) {
            System.out.println("palindrome");
        }
    }
}
