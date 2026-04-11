package com.streams.strings.advanced;

import java.util.Arrays;
import java.util.stream.Collectors;

//reverse each word in a string
//input: hello world
//output: olleh dlrow
public class Problem3 {

    public static void main(String[] args) {

        String str = "hello world";

        //reduce
        String rev1 = Arrays.stream(str.split(" "))
                .map(n -> n.chars().mapToObj(i -> String.valueOf((char) i)).reduce("", (a, b) -> b + a))
                .collect(Collectors.joining(" "));
        System.out.println(rev1);
    }
}
