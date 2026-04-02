package com.basic.array;

import java.util.Arrays;

//strings anagram or not
public class Problem2 {

    public static void main(String[] args) {
        String str1 = "heart";
        String str2 = "earth";

        char[] arr1 = str1.toCharArray();
        char[] arr2 = str2.toCharArray();

        Arrays.sort(arr1);
        Arrays.sort(arr2);

        if(Arrays.equals(arr1, arr2)) {
            System.out.println("anagram");
        }
    }
}