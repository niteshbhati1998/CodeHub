package com.basic.array;

//find missing number
//if O is there in input no need to do arr.length+1
public class Problem1 {
    public static void main(String[] args) {
        int[] arr = {9,6,4,2,3,5,7,1};

        int n = arr.length+1;
        int expectedSum = (n*(n+1))/2;

        int sum = 0;
        for(int i=0;i<arr.length;i++) {
            sum+=arr[i];
        }

        int missingNum = expectedSum-sum;
        System.out.println(missingNum);

    }
}
