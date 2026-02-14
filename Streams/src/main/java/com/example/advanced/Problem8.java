package com.example.advanced;

import java.util.stream.Stream;

public class Problem8 {
    public static void main(String[] args) {
        int a = 5;

        //works
        Stream.of(a).forEach(n->System.out.println(a + 1));

        //exception: local variables referenced from a lambda expression must be final or effectively final
        //Stream.of(a).forEach(n->System.out.println(a++));
    }
}
