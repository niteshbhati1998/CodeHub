package com.streams.strings.advanced;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

//input="aabbbcc"
//output=b3a2c2
public class Problem7 {
    public static void main(String[] args) {

        String str = "aabbbcc";

        String val = str.chars()
                .mapToObj(n -> (char) n)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted((a,b)->Long.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b)->b, LinkedHashMap::new))
                .entrySet()
                .stream()
                .map(n->n.getKey()+""+n.getValue())
                .collect(Collectors.joining());
        System.out.println(val);
    }
}
