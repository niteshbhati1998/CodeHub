package com.streams.list.basic;

import java.util.Arrays;
import java.util.List;

//print all numbers starting with 1
public class Problem5 {
    public static void main(String[] args) {

        List<Integer> list = Arrays.asList(10, 20, 15, 25, 30);

        list.stream().filter(n -> n.toString().startsWith("1")).forEach(System.out::println);
    }
}
