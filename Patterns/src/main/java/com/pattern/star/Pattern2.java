package com.pattern.star;

/*
    *
   **
  ***
 ****
*****
 */
public class Pattern2 {
    public static void main(String[] args) {

        int n = 5;

        //approach-1
        for (int i = 1; i <= n; i++) {

            //space
            for (int j = i; j < n; j++) {
                System.out.print(" ");
            }

            //fill *
            for (int j = 1; j <= i; j++) {
                System.out.print("*");
            }
            System.out.println();
        }

        //approach-2
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                int val = n - (i - 1);
                if (j == val) {
                    for (int k = val; k <= n; k++) {
                        System.out.print("*");
                    }
                } else {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }
}
