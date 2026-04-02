package com.basic.array;

//reverse given string
public class Problem4 {

    public static void main(String[] args) {
        String str = "hello";

        //string builder
        String rev = new StringBuilder(str).reverse().toString();
        System.out.println(rev);

        //loop
        String rev1 = "";
        for (int i = str.length() - 1; i >= 0; i--) {
            rev1 += str.charAt(i);
        }
        System.out.println(rev1);

        //recursion
        String rev2 = reverseString(str);
        System.out.println(rev2);

        //two-pointer
        String rev3 = twoPointer(str);
        System.out.println(rev3);
    }

     static String reverseString(String str) {
        if(str.length()==0) {
            return str;
        }
        return str.charAt(str.length()-1) + reverseString(str.substring(0, str.length()-1));
    }

    static String twoPointer(String str) {

        char[] arr = str.toCharArray();

        int start = 0;
        int end = str.length()-1;
        while(start<end) {
            char temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;

            start++;
            end--;
        }
        return new String(arr);
    }
}