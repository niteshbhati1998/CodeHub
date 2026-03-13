package com.streams.list.basic;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

//find max element from list
public class Problem2 {
    public static void main(String[] args) {

        List<Integer> list = Arrays.asList(50, 70, 30, 10, 40);

        //sorting
        Optional<Integer> max = list.stream().sorted((a, b) -> b - a).findFirst();
        System.out.println(max.get());

        //IntStream
        OptionalInt max1 = list.stream().mapToInt(Integer::intValue).max();
        System.out.println(max1.getAsInt());

        //reduce
        int max2 = list.stream().reduce(Integer.MIN_VALUE, (a, b) -> Integer.max(a, b));
        System.out.println(max2);
    }
}
