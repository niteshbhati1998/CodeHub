package com.streams.list.advanced;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

//find duplicate elements from list
public class Problem1 {

    public static void main(String[] args) {

        List<Integer> list = Arrays.asList(50, 70, 30, 50, 10, 40, 30, 50, 30);

        //HashSet
        Set<Integer> set = new HashSet<>();
        list.stream()
                .filter(n -> !set.add(n))
                .distinct()
                .forEach(System.out::println);

        //Collections
        list.stream()
                .filter(n -> Collections.frequency(list, n) > 1)
                .distinct()
                .forEach(System.out::println);

        //Map
        list.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(n -> n.getValue() > 1)
                .map(Map.Entry::getKey)
                .forEach(System.out::println);

        //Array
        int[] arr = {1, 2, 3, 1, 2, 1};
        Arrays.stream(arr)
                .boxed()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(n -> n.getValue() == 1)
                .map(Map.Entry::getKey)
                .forEach(System.out::println);
    }
}
