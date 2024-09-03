package com.kg.algorithms;

import java.util.logging.Level;
import lombok.extern.java.Log;

@Log
public class BubbleSort {

    private static int[] randomInt(int size) {
        int[] list = new int[size];
        for (int i=0; i<size; i++) {
            list[i] = Math.round((int) (Math.random() * 100));
        }
        return list;
    }

    private static void bubbleSort(int[] array) {
        int n = array.length;
        boolean swapped = false;

        if (n <= 1) {
            log.log(Level.INFO, "This is an empty array or sorted array");
            return;
        }

        // the 1st loop is the number of overall loop
        for (int i = 0; i < n - 1; i++) {
            // the 2nd loop is to swap the closest two elements in the array
            for (int j = 0; j < n - i - 1; j++) {
                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }

    public static void sort(int[] array) {
        bubbleSort(array);
    }

    private static void printArray(String message, int[] array) {
        System.out.print(message + ": ");
        for (int i: array) {
            System.out.print(i + " ");
        }
    }

    public static void main(String[] args) {
        log.log(Level.INFO, "Start a bubble sort algorithm");
        int[] arr = randomInt(20);
        printArray("Before sorting", arr);
        sort(arr);
        printArray("After sorting", arr);
    }

}
