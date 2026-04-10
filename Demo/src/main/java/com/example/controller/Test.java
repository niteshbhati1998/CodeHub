package com.example.controller;

import java.util.stream.Collectors;

public class DebuggingChallenge {

    public static void main(String[] args) {
        String str = "Apple";

        String rev = str.chars()
                .mapToObj(n-> String.valueOf((char)n))
                .sorted()
                .collect(Collectors.joining());

    }
}
