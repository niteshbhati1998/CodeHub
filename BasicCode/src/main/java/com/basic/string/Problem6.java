package com.basic.string;

import java.util.HashMap;
import java.util.Map;

//count occurrence of each character in string
public class Problem6 {

    public static void main(String[] args) {
        String str = "aaaaabbbb2222ccc111dde";

        Map<Character, Integer> map = new HashMap<>();
        char[] arr = str.toCharArray();
        for (char val : arr) {
            map.put(val, map.getOrDefault(val, 0)+1);
        }
        System.out.println(map);
    }
}
