package com.basic.string;

import java.util.HashSet;
import java.util.Set;

//longest substring length without repeated characters
//TC O(n^3)
public class Problem8 {

    public static void main(String[] args) {
        String str = "studestrepdefj123567899";

        Set<String> set = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            sb.append(str.charAt(i));
            for (int j = i + 1; j < str.length(); j++) {
                String val = String.valueOf(str.charAt(j));
                if (sb.toString().contains(val)) {
                    break;
                } else {
                    sb.append(val);
                }
            }
            set.add(sb.toString());
            sb = new StringBuilder();
        }
        System.out.println(set);

        int max = 0;
        String sub = "";
        for (String s : set) {
            if (s.length() > max) {
                max = s.length();
                sub = s;
            }
        }
        System.out.println(max);
        System.out.println(sub);
    }
}
