package com.basic.string;

import java.util.*;

//find all characters having second highest occurrence in string
public class Problem7 {

    public static void main(String[] args) {
        String str = "aaaaabbbb2222ccc111dde";

        Map<Character, Integer> map = new HashMap<>();
        char[] arr = str.toCharArray();
        for (char val : arr) {
            map.put(val, map.getOrDefault(val, 0)+1);
        }
        System.out.println(map);

        List<Integer> list = new ArrayList<>(new HashSet<>(map.values()));
        Collections.sort(list);
        int secondHighest = list.get(list.size()-2);

        map.forEach((k,v)-> {
            if(v.equals(secondHighest)) {
                System.out.println(k);
            }
        });
    }
}