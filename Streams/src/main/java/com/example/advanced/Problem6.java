package com.example.advanced;

import java.util.Arrays;
import java.util.stream.Collectors;

//reverse character of each word
//input: hello world
//output: olleh dlrow
public class Problem6 {
    public static void main(String[] args) {
        String str = "hello world";

        //StringBuilder
        String rev = Arrays.stream(str.split(" "))
                .map(n -> new StringBuilder(n).reverse())
                .collect(Collectors.joining(" "));
        System.out.println(rev);

        //reduce
        //split("") uses regex, so "abc".split("") → ["", "a", "b", "c"]
        String rev1 = Arrays.stream(str.split(" "))
                .map(n->Arrays.stream(n.split("")).reduce("", (a,b)-> b+a))
                .collect(Collectors.joining(" "));
        System.out.println(rev1);
    }
}
