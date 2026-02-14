package com.example.basic;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

//sort map based on value in descending order
public class Problem9 {
    public static void main(String[] args) {

        //used linked hashmap to preserve sorting order
        //used merge function: if same keys are present, which key value to consider (a,b)->a
        Map<String, Integer> map = new HashMap<>();
        map.put("A", 10);
        map.put("B", 50);
        map.put("C", 70);
        map.put("A", 30);

        Map<String, Integer> val = map.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
        System.out.println(val);

        //comparator output type must be int
        //Long.compare safely compares two long values and returns the required int
        Map<String, Long> map1 = new HashMap<>();
        map1.put("A", 10L);
        map1.put("B", 50L);
        map1.put("C", 70L);
        map1.put("A", 30L);

        Map<String, Long> val1 = map1.entrySet()
                .stream()
                //.sorted((a, b) -> (int) (b.getValue() - a.getValue()))
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
        System.out.println(val1);
    }
}
