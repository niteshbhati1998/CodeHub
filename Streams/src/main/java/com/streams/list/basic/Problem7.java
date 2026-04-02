package com.streams.list.basic;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

//find average of all numbers
public class Problem7 {
    public static void main(String[] args) {

        List<Integer> list = Arrays.asList(50, 70, 30, 10, 40);

        OptionalDouble val = list.stream().mapToDouble(Integer::doubleValue).average();
        System.out.println(val.getAsDouble());
    }
}
