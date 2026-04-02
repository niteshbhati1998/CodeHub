package com.basic.string;

import java.util.*;

//longest substring without repeated characters
public class Problem8 {

    public static void main(String[] args) {
        String str = "bbbbb";
        char[] arr = str.toCharArray();

        List<String> list = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        for(int i=0;i<arr.length-1;i++) {
            sb.append(arr[i]);
            for(int j=i+1;j<arr.length;j++) {

                if(sb.toString().contains(String.valueOf(arr[j]))) {
//                    map.put(subString.toString(), subString.size());
//                    subString = new LinkedHashSet<>();
                    break;
                }
                sb.append(arr[j]);
            }
            list.add(sb.toString());
            sb = new StringBuilder();
        }
        System.out.println(list);

        int count = 0;
        String key = "";
        for(String strr: list) {
            if(strr.length()>count) {
                count = strr.length();
                key = strr;
            }
        }
        System.out.println(key);
    }
}
