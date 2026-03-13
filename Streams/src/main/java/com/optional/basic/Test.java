package com.optional.basic;

import java.util.Optional;

public class Test {
    public static void main(String[] args) {

        String str = "hello";
        String str1 = null;

        //create Optional
        Optional<Object> val = Optional.empty();
        System.out.println(val.get());                         //Exception: No value present

        Optional<String> val1 = Optional.of(str);
        System.out.println(val1.get());

        Optional<String> val2 = Optional.of(str1);             //Exception: NullPointerException

        Optional<String> val3 = Optional.ofNullable(str);
        System.out.println(val3.get());

        Optional<String> val4 = Optional.ofNullable(str1);
        System.out.println(val4.get());                        //Exception: No value present

    }
}
