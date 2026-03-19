package com.streams.strings.advanced;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//reverse character of each word
//input: hello world
//output: olleh dlrow
public class Problem3 {

    public static void main(String[] args) {

        String str = "hello world";

        //IntStream
        String rev = Arrays.stream(str.split(" "))
                .map(n -> IntStream.range(0, n.length()).mapToObj(i -> String.valueOf(n.charAt(n.length() - 1 - i))).collect(Collectors.joining()))
                .collect(Collectors.joining(" "));
        System.out.println(rev);

        //reduce
        String rev1 = Arrays.stream(str.split(" "))
                .map(n -> n.chars().mapToObj(i -> String.valueOf((char) i)).reduce("", (a, b) -> b + a))
                .collect(Collectors.joining(" "));
        System.out.println(rev1);
    }
}
