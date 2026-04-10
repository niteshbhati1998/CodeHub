package com.basic.string;

import java.util.HashSet;
import java.util.Set;

//longest substring length without repeated characters
//sliding window O(n)
public class Problem9 {

    public static void main(String[] args) {
        String str = "abcdabcab";

        int left = 0;
        int right = 0;
        int max = 0;

        Set<Character> set = new HashSet<>();
        while(right<str.length()) {

            char c = str.charAt(right);

            while(set.contains(c)) {
                set.remove(str.charAt(left));   //abcb
                left++;
            }
            set.add(c);

            if(right-left+1>max) {
                max = right-left+1;
            }
            right++;
        }
        System.out.println(max);
    }
}
