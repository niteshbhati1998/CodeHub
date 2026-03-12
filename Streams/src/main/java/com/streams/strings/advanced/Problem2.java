package com.streams.strings.advanced;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

//string palindrome or not (string == reverse string)
public class Problem2 {
    public static void main(String[] args) {

        String str = "madam";

        //two-pointer technique
        boolean b = IntStream.range(0, str.length() / 2)
                .allMatch(n -> str.charAt(n) == str.charAt(str.length() - 1 - n));
        if (b) {
            System.out.println("palindrome");
        }

        //IntStream
        boolean b1 = IntStream.range(0, str.length())
                .mapToObj(n -> String.valueOf(str.charAt(str.length() - 1 - n)))
                .collect(Collectors.joining())
                .equals(str);
        if (b1) {
            System.out.println("palindrome");
        }
    }
}
