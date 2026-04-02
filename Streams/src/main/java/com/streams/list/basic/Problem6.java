package com.streams.list.basic;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//input: ["542", "42", "63", "540"]
//output: 5425406342
public class Problem6 {
    public static void main(String[] args) {

        List<String> list = Arrays.asList("542", "42", "63", "540");

        String str = list.stream()
                .map(Integer::valueOf)
                .sorted((a, b) -> b - a)
                .map(Object::toString)
                .collect(Collectors.joining());
        System.out.println(str);
    }
}
