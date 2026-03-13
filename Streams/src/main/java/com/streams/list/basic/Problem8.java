package com.streams.list.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//find common elements between two lists
public class Problem8 {
    public static void main(String[] args) {
        List<Integer> list1 = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
        List<Integer> list2 = new ArrayList<>(Arrays.asList(7, 8, 3, 4, 9, 10));

        list1.stream()
                .filter(list2::contains)
                .forEach(System.out::println);
    }
}
