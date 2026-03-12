package com.streams.basic;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

//find second-largest element from list
public class Problem3 {
    public static void main(String[] args) {

        List<Integer> list = Arrays.asList(50, 70, 30, 10, 40);

        Optional<Integer> val = list.stream().sorted((a, b) -> b - a).skip(1).findFirst();
        System.out.println(val.get());
    }
}
