package com.streams.strings.advanced;

import java.util.function.Function;
import java.util.stream.Collectors;

//input="aabbbcc"
//output=b3a2c2
public class Problem6 {
    public static void main(String[] args) {

        String str = "aabbbcc";

        String val = str.chars()
                .mapToObj(n -> (char) n)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted((a,b)->Long.compare(b.getValue(), a.getValue()))
                .map(n->n.getKey()+""+n.getValue())   //use "" else -> char + long = long
                .collect(Collectors.joining());
        System.out.println(val);
    }
}
