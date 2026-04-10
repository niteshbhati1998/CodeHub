package com.basic.string;

import java.util.*;

//group anagrams together
//input: [“eat”, “tea”, “tan”, “ate”, “nat”, “bat”]
//output: [[“bat”], [“nat”, “tan”], [“ate”, “eat”, “tea”]]
public class Problem3 {

    public static void main(String[] args) {
        List<String> list = new ArrayList<>(Arrays.asList("eat", "tea", "tan", "ate", "nat", "bat"));

        Map<String, List<String>> map = new HashMap<>();
        for (String str : list) {
            char[] arr = str.toCharArray();
            Arrays.sort(arr);
            String sorted = Arrays.toString(arr);

            if(map.containsKey(sorted)) {
                List<String> list1 = new ArrayList<>(map.get(sorted));
                list1.add(str);
                map.put(sorted, list1);
            } else {
                List<String> list1 = new ArrayList<>();
                list1.add(str);
                map.put(sorted, list1);
            }
        }

        List<List<String>> finalList = new ArrayList<>();
        map.forEach((k,v) -> {
            finalList.add(v);
        });
        System.out.println(finalList);
    }
}