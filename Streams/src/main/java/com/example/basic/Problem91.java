package com.example.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//find common elements between two lists
public class Problem91 {
    public static void main(String[] args) {
        List<Integer> list1 = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));
        List<Integer> list2 = new ArrayList<>(Arrays.asList(7, 8, 3, 4, 9, 10));

        //modifying list
        //list1.addAll(list2);
        //Set<Integer> hs = new HashSet<>();
        //list1.stream().filter(n-> !hs.add(n)).forEach(System.out::println);

        //without modifying list
        list1.stream().filter(list2::contains).forEach(System.out::println);
    }
}
