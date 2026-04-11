package com.streams.strings.basic;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//group anagrams together
//input: ["eat", "tea", "tan", "ate", "nat", "bat"]
//output: [["bat"], ["nat", "tan"], ["ate", "eat", "tea"]]
public class Problem3 {
    public static void main(String[] args) {

        List<String> list = Arrays.asList("eat", "tea", "tan", "ate", "nat", "bat");

        //groupingBy needs a classifier function, key for grouping -> value is always List<?>
        Map<String, List<String>> map = list.stream()
                .collect(Collectors.groupingBy(n -> Arrays.stream(n.split("")).sorted().collect(Collectors.joining())));

        List<List<String>> anagramList = map.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        System.out.println(anagramList);
    }
}