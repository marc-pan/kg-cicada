package com.kg.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MergeArrays {
    public static void merge(int[] array1, int m, int[] array2, int n) {
        Arrays.sort(array1);
        Arrays.sort(array2);

        if (m == 0) {
            array1 = array2;
            return;
        }

        if (n == 0) {
            return;
        }

        List<Integer> arrayList = new ArrayList<Integer>();
        for (int i=0; i<m; i++) {
            arrayList.add(array1[i]);
        }
        for (int i=0; i<n; i++) {
            arrayList.add(array2[i]);
        }

        arrayList.removeIf(i -> i ==0);

        array1 = arrayList.stream().mapToInt(i -> i).toArray();
    }

    public static void main(String[] args) {
        int[] nums1 = {1,2,3,0,0,0};
        int[] nums2 = {2,5,6};
        merge(nums1, nums1.length, nums2, nums2.length);
        for (int i=0; i<nums1.length; i++) {
            System.out.print(nums1[i]);
        }
    }
}
