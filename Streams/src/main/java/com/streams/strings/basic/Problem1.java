package com.streams.strings.basic;

import java.util.Arrays;
import java.util.stream.Collectors;

//sort the string in ascending order
public class Problem1 {
    public static void main(String[] args) {

        String str = "Apple";

        String str1 = str.chars()
                .mapToObj(n -> String.valueOf((char) n))
                .sorted()
                .collect(Collectors.joining());
        System.out.println(str1);

        String str2 = Arrays.stream(str.split(""))
                .sorted()
                .collect(Collectors.joining());
        System.out.println(str2);
    }
}