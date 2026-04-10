package com.streams.strings.basic;

import java.util.stream.Collectors;

//two strings anagram or not (same character, different order)
public class Problem2 {

    public static void main(String[] args) {

        String str1 = "silent";
        String str2 = "listen";

        String val1 = str1.chars()
                .mapToObj(n -> String.valueOf((char) n))
                .sorted()
                .collect(Collectors.joining());

        String val2 = str2.chars()
                .mapToObj(n -> String.valueOf((char) n))
                .sorted()
                .collect(Collectors.joining());

        if (val1.equals(val2)) {
            System.out.println("anagram");
        }
    }
}
