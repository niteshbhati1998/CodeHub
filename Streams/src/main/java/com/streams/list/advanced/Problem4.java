package com.streams.list.advanced;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//collect all unique emails into a list
//emails should be valid, no null, no empty
public class Problem4 {
    public static void main(String[] args) {

        Map<String, List<String>> map = new HashMap<>();
        map.put("A", Arrays.asList("123@example.com", "234@example.com"));
        map.put("B", Arrays.asList());
        map.put("C", Arrays.asList("123@example.com"));
        map.put("D", Arrays.asList("456@example.com", "jahshshhs"));
        map.put("E", null);

        List<String> list = map.entrySet()
                .stream()
                .filter(n -> n.getValue() != null)
                .flatMap(n -> n.getValue().stream())
                .filter(n -> n.contains("@") && n.contains(".com"))
                .collect(Collectors.toList());
        System.out.println(list);
    }
}