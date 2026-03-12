package com.streams.strings.advanced;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//reverse a string
public class Problem1 {
    public static void main(String[] args) {

        String str = "hello";

        //StringBuilder
        Optional<String> rev = Stream.of(str)
                .map(n -> new StringBuilder(n).reverse().toString())
                .findFirst();
        System.out.println(rev.get());

        //IntStream
        String rev1 = IntStream.range(0, str.length())
                .mapToObj(n -> String.valueOf(str.charAt(str.length() - 1 - n)))
                .collect(Collectors.joining());
        System.out.println(rev1);

        //reduce
        String rev2 = Arrays.stream(str.split(""))
                        .reduce("", (a,b)->b+a);
        System.out.println(rev2);
    }
}
