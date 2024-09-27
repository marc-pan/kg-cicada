package com.kg.algorithms;

import java.util.Arrays;

public class MergeArrays {
    public static void merge(int[] array1, int m, int[] array2, int n) {
        Arrays.sort(array1);
        Arrays.sort(array2);

        if (m == 0) {
            System.arraycopy(array2, 0, array1, 0, array1.length);
            return;
        }

        if (n == 0) {
            return;
        }

        for (int i=0; i<n; i++) {
            array1[m+i] = array2[i];
        }

        Arrays.sort(array1);
        print(array1);
    }

    public static void print(int[] array) {
        for (int i=0; i<array.length; i++) {
            System.out.print(array[i]);
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int[] nums1 = {1,2,3,0,0,0};
        int[] nums2 = {2,5,6};
        merge(nums1, 3, nums2, 3);
        print(nums1);
    }

}
