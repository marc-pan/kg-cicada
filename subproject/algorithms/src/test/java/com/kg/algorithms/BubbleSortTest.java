package com.kg.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BubbleSortTest {
    private static int[] prepareData(int size) {
        int[] list = new int[size];
        for (int i=0; i<size; i++) {
            list[i] = Math.round((int) (Math.random() * 100));
        }
        return list;
    }

    @Test
    public void testBubbleSort() {
        int[] data = prepareData(20);
        BubbleSort.sort(data);
        assertEquals(20, data.length);
    }
}
