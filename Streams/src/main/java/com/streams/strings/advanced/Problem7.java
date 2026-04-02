package com.streams.strings.advanced;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

//find all characters with second highest count
public class Problem7 {

    public static void main(String[] args) {
        String str = "aaaaabbbbeeeffghhhhiiii";

        //Collection.sort(List<Character>, char) ->correct
        //Collection.sort(List<String>, char)    ->wrong -> thatswhy can't use Arrays.stream()
        Map<Integer, List<Character>> map = str.chars()
                .mapToObj(n -> (char) n)
                .collect(Collectors.groupingBy(n -> Collections.frequency(str.chars().mapToObj(i -> (char) i).collect(Collectors.toList()), n)));
        System.out.println(map);

        Optional<List<Character>> map1 = map.entrySet()
                .stream()
                .sorted((a,b)->b.getKey()-a.getKey())
                .skip(1)
                .map(n->n.getValue().stream().distinct().collect(Collectors.toList()))
                .findFirst();
        System.out.println(map1.get());
    }
}
