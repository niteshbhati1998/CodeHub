package com.streams.list.basic;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//input: [542, 42, 63, 540]
//output: 5425406342
public class Problem7 {
    public static void main(String[] args) {

        List<Integer> list = Arrays.asList(542, 42, 63, 540);

        String str = list.stream()
                .sorted((a, b) -> b - a)
                .map(Object::toString)
                .collect(Collectors.joining());
        System.out.println(str);
    }
}
