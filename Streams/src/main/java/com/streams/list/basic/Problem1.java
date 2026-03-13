package com.streams.list.basic;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

//find min element from list
public class Problem1 {
    public static void main(String[] args) {

        List<Integer> list = Arrays.asList(50, 70, 30, 10, 40);

        //sorting
        Optional<Integer> min = list.stream().sorted().findFirst();
        System.out.println(min.get());

        //IntStream
        OptionalInt min1 = list.stream().mapToInt(Integer::intValue).min();
        System.out.println(min1.getAsInt());

        //reduce
        int min2 = list.stream().reduce(Integer.MAX_VALUE, (a, b) -> Integer.min(a, b));
        System.out.println(min2);
    }
}
