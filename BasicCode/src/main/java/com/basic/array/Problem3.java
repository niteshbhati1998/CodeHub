package com.basic.array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//find the number which is not having any pair
public class Problem3 {
    public static void main(String[] args) {
        int[] arr = {1,2,3,1,2};

        int result = 0;
        for(int i=0;i<arr.length;i++) {
            result^=arr[i];
        }
        System.out.println(result);
    }
}
