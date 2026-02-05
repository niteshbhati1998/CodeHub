package com.example.advanced;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

//find first non-repeating character in a given string, print -1 if not available
//str = "aabbcddeef"
//output: c
public class Problem1 {

    public static void main(String[] args) {
        String str = "aabbcddeef";

        Optional<Character> val = str.chars()
                .mapToObj(n-> (char)n)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(n->n.getValue()==1)
                .map(n->n.getKey())
                .findFirst();
        System.out.println(val.map(String::valueOf).orElse("-1"));

        //concise but less efficient
        Optional<Character> val1 = str.chars()
                .mapToObj(n-> (char)n)
                .filter(n->str.indexOf(n)==str.lastIndexOf(n))
                .findFirst();
        System.out.println(val1.map(String::valueOf).orElse("-1"));
    }
}