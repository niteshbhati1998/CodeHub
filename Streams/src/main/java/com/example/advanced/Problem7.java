package com.example.advanced;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

//input="aabbbcc"
//output=b3a2c2
public class Problem7 {
    public static void main(String[] args) {

        String str = "aabbbcc";

        Map<Character, Long> map = str.chars().mapToObj(n -> (char) n)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a,b)->a, LinkedHashMap::new));

        String val = map.entrySet()
                .stream()
                .map(n->n.getKey()+""+n.getValue())
                .collect(Collectors.joining());

        System.out.println(val);

    }
}
