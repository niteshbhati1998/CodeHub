package com.streams.list.basic;

import java.util.Arrays;
import java.util.List;

//find distinct elements from list
public class Problem6 {
    public static void main(String[] args) {

        List<Integer> list = Arrays.asList(50, 70, 30, 50, 10, 40, 30, 50, 30);

        list.stream().distinct().forEach(System.out::println);
    }
}
