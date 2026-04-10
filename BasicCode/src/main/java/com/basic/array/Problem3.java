package com.basic.array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//find nth missing numbers
public class Problem2 {
    public static void main(String[] args) {
        int[] arr = {5,1,4,7,2};

        Arrays.sort(arr);
        List<Integer> list = new ArrayList<>();

        for(int i=0;i<arr.length-1;i++) {
            int current = arr[i];
            int next = arr[i+1];

            if(next-current>1) {
                for(int j=current+1;j<next;j++) {
                    list.add(j);
                }
            }
        }
        System.out.println(list);
    }
}
