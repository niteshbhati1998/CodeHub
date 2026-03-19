package com.basic.string;

import java.util.*;
import java.util.stream.Collectors;

//group anagrams together
//input: [“eat”, “tea”, “tan”, “ate”, “nat”, “bat”]
//output: [[“bat”], [“nat”, “tan”], [“ate”, “eat”, “tea”]]
public class Problem2 {
    public static void main(String[] args) {

        List<String> list = new ArrayList<>(Arrays.asList("eat", "tea", "tan", "ate", "nat", "bat"));

        Map<String, List<String>> map = new HashMap<>();
        for (String str : list) {
            String sorted = Arrays.stream(str.split("")).sorted().collect(Collectors.joining());

            if (map.containsKey(sorted)) {
                List<String> prevData = map.get(sorted);
                prevData.add(str);
                map.put(sorted, prevData);
            } else {
                map.put(sorted, new ArrayList<>(List.of(str)));
            }
        }

        List<List<String>> finalList = new ArrayList<>();
        map.forEach((k, v) -> {
            finalList.add(v);
        });
        System.out.println(finalList);
    }
}
