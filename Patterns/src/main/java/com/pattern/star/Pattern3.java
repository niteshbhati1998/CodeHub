package com.pattern.star;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
    *
   * *
  * * *
 * * * *
* * * * *
 */
public class Pattern3 {
    public static void main(String[] args) {

        int n = 5;

        //approach-1
        for (int i = 1; i <= n; i++) {

            //space
            for(int j=i;j<n;j++) {
                System.out.print(" ");
            }

            //fill *
            for(int k=1;k<=i;k++) {
                System.out.print("* ");
            }
            System.out.println();
        }

        //approach-2
        List<Integer> list = new ArrayList<>(Arrays.asList(n));
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j < n * 2; j++) {
                if (list.contains(j)) {
                    System.out.print("*");
                } else {
                    System.out.print(" ");
                }
            }
            System.out.println();

            //update list
            List<Integer> list1 = new ArrayList<>();
            for (int k = 0; k < list.size(); k++) {
                list1.add(list.get(k) - 1);
                if (k == list.size() - 1) {
                    list1.add(list.get(k) + 1);
                }
            }
            list = new ArrayList<>(list1);
        }
    }
}


