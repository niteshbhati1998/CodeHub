package com.streams.approach;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Approach {
    public static void main(String[] args) {

        //apply stream over string
        String str = "hello";

        //chars() -> IntStream -> Stream<Character>
        str.chars()
                .mapToObj(n -> (char) n)
                .forEach(System.out::println);

        //split() -> Stream<String>
        Arrays.stream(str.split(""))
                .forEach(System.out::println);

        //collecting as a map
        Map<String, Integer> map = new HashMap<>();
        map.put("C", 10);
        map.put("A", 70);
        map.put("B", 50);
        map.put("A", 30);
        map.put("D", 10);

        //groupBy -> single value -> Stream<Integer> / Stream<String>
        Map<Integer, Long> map1 = map.values()
                .stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        System.out.println(map1);

        //hashmap to hashmap
        Map<String, String> map3 = map.entrySet()
                .stream()
                .collect(Collectors.toMap(n -> n.getKey(), n -> String.valueOf(n.getValue())));
        System.out.println(map3);

        //hashmap to linkedHashMap
        //merge function: (a, b) -> b
        Map<String, Integer> map4 = map.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
        System.out.println(map4);

        //list to map
        List<String> list = Arrays.asList("a", "b", "c", "a");
        Map<String, Integer> map5 = list.stream()
                .collect(Collectors.toMap(n->n, n->1, (a,b) -> b));
        System.out.println(map5);

        //string to map
        Map<Character, String> map6 = str.chars()
                .mapToObj(n -> (char) n)
                .collect(Collectors.toMap(n->n, n->"1", (a,b)->b));
        System.out.println(map6);

        //string to map with group by
        Map<Character, Long> map7 = str.chars()
                .mapToObj(n -> (char) n)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        System.out.println(map7);

        //string to map with group by with LinkedHashMap
        Map<Character, Long> map8 = str.chars()
                .mapToObj(n -> (char) n)
                .sorted((a, b) -> b - a)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
        System.out.println(map8);

    }
}
