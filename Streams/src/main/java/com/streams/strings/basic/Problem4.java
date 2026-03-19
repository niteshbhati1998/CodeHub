package com.streams.strings.basic;

//check string contains vowel or not
public class Problem4 {
    public static void main(String[] args) {

        String str = "Apple";
        boolean b = str.chars()
                .mapToObj(n -> (char) n)
                .map(Character::toLowerCase)
                .anyMatch(n -> n == 'a' || n == 'i' || n == 'o' || n == 'u');
        System.out.println(b);
    }
}