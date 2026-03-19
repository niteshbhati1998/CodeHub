package com.streams.strings.advanced;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//find unique words from given string array
//input: [“abc”, “dde”, “ffg”, “ijk”]
//output: ["abc", "ijk"]
public class Problem5 {

    public static void main(String[] args) {
        List<String> list = new ArrayList<>(Arrays.asList("abc", "dde", "ffg", "ijk"));

        //set
        List<String> list1 = list.stream()
                .filter(n -> n.chars().mapToObj(i -> (char) i).collect(Collectors.toSet()).size() == n.length())
                .collect(Collectors.toList());
        System.out.println(list1);
    }
}