package com.basic.string;

import java.util.Arrays;

//sort string in ascending order
public class Problem1 {

    public static void main(String[] args) {
        String str = "hello";

        char[] arr = str.toCharArray();
        Arrays.sort(arr);
        String sorted = new String(arr);

        System.out.println(sorted);
    }
}
