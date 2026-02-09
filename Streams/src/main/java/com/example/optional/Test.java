package com.example.optional;

import java.util.Optional;

public class Test {
    public static void main(String[] args) {
        //Optional<String> value = Optional.empty();
        //System.out.println(value.get());            //Exception: No value present

        //Optional<String> value1 = Optional.of(null);
        //System.out.println(value1.get());           //Exception: NullPointerException

        //Optional<String> value2 = Optional.ofNullable(null);
        //System.out.println(value2.orElseThrow());             //Exception: NoValuePresent

//        Optional<String> value3 = Optional.ofNullable(null);
//        value3.ifPresent(n->System.out.println(n));

        Optional<String> value4 = Optional.ofNullable(null);
        System.out.println(value4.orElse(2));

    }
}
