package com.streams.strings.basic;

import java.util.Arrays;
import java.util.stream.Collectors;

//two strings anagram or not (same character, different order)
public class Problem2 {

    public static void main(String[] args) {

        String str1 = "silent";
        String str2 = "listen";

        String val1 = Arrays.stream(str1.split(""))
                .sorted()
                .collect(Collectors.joining(""));

        String val2 = Arrays.stream(str2.split(""))
                .sorted()
                .collect(Collectors.joining(""));

        if (val1.equals(val2)) {
            System.out.println("anagram");
        }
    }
}
