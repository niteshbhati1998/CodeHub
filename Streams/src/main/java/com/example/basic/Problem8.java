package com.example.basic;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//concatenate list of integers in their decreasing order
//input: [542, 42, 63, 540]
//output: 5425406342
public class Problem8 {
    public static void main(String[] args) {

        List<Integer> list = Arrays.asList(542, 42, 63, 540);

        String str = list.stream()
                .sorted((a, b) -> b - a)
                .map(String::valueOf)
                .collect(Collectors.joining());
        System.out.println(str);
    }
}
