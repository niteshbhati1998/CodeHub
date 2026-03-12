package com.streams.basic;

import java.util.Arrays;

//check string contains vowel or not
public class Problem8 {
    public static void main(String[] args) {

        String str = "Apple";
        boolean val = Arrays.stream(str.split(""))
                .map(String::toLowerCase)
                .anyMatch(n -> n.equals("a") || n.equals("e") || n.equals("i") || n.equals("o") || n.equals("u"));
        System.out.println(val);


        boolean b  = str.chars()
                .mapToObj(n -> (char) n)
                .map(Character::toLowerCase)
                .anyMatch(n->n=='a' || n=='i' || n=='o' || n=='u');
        System.out.println(b);
    }
}