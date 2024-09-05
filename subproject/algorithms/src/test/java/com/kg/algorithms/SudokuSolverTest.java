package com.kg.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

public class SudokuSolverTest {
    private static final int SIZE = 9;
    private static final int EMPTY = 0;
    private static final int NUM_COUNT = SIZE * SIZE;
    private static final int EMPTY_COUNT = NUM_COUNT - SIZE; // 77 is a common number for a challenging game

    private static int[][] generateSudoku(int emptyCount) {
        int[][] board = new int[SIZE][SIZE];
        SudokuSolver.solveSudoku(board);
        SudokuSolver.printBoard(board);
        System.out.println();
        removeNumbers(board, emptyCount);
        return board;
    }

    private static void removeNumbers(int[][] board, int emptyCount) {
        Random rand = new Random();
        int localEmptyCount = emptyCount == 0? EMPTY_COUNT: emptyCount;
        while (localEmptyCount > 0) {
            int row = rand.nextInt(SIZE);
            int col = rand.nextInt(SIZE);
            if (board[row][col] != EMPTY) {
                board[row][col] = EMPTY;
                localEmptyCount--;
            }
        }
    }

    @Test
    public void testSudoku() {
        int[][] board = generateSudoku(45);
        int[][] beforeBoard = board;
        SudokuSolver.solveSudoku(board);
        
        SudokuSolver.printBoard(board);
        assertEquals(beforeBoard, board);
    }

    @Test
    public void testFixedSudoku() {
        int[][] board = {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };
        int[][] beforeBoard = board;
        SudokuSolver.solveSudoku(board);
        
        SudokuSolver.printBoard(board);
        assertEquals(beforeBoard, board);

    }
}
