package com.streams.basic;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

//sort map based on value in descending order
public class Problem9 {
    public static void main(String[] args) {

        Map<String, Long> map = new HashMap<>();
        map.put("A", 10L);
        map.put("B", 50L);
        map.put("C", 70L);
        map.put("A", 30L);

        Map<String, Long> map1 = map.entrySet()
                .stream()
                .sorted((a,b)->Long.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a,b)-> b, LinkedHashMap::new));
        System.out.println(map1);
    }
}
