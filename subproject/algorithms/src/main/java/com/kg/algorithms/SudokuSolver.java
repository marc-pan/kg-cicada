package com.kg.algorithms;

import java.util.logging.Level;

import lombok.extern.java.Log;

@Log
public class SudokuSolver {
    private static final int SIZE = 9;

    /**
     * Fill the number (1 ~ 9) into the board
     *
     * @param board 9x9 board, each cell is a number of 1 ~ 9
     * @return 
     */
    public static boolean solveSudoku(int[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    // insert the number from 1 to 9 if it's zero
                    // TODO: shuffle the number
                    for (int number = 1; number <= SIZE; number++) {
                        if (isValid(board, row, col, number)) {
                            board[row][col] = number;
                            if (solveSudoku(board)) {
                                return true;
                            } else {
                                board[row][col] = 0; // Backtrack
                            }
                        }
                    }

                    return false; // Trigger backtracking
                }
            }
        }

        return true; // Solution found
    }
    
    /**
     * 
     *
     * @param board
     * @param row
     * @param col
     * @param number
     * @return
     */
    private static boolean isValid(int[][] board, int row, int col, int number) {
        // Check row
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == number) {
                return false;
            }
        }

        // Check column
        for (int i = 0; i < SIZE; i++) {
            if (board[i][col] == number) {
                return false;
            }
        }

        // Check 3x3 box
        int localBoxRow = row - row % 3;
        int localBoxCol = col - col % 3;
        for (int i = localBoxRow; i < localBoxRow + 3; i++) {
            for (int j = localBoxCol; j < localBoxCol + 3; j++) {
                if (board[i][j] == number) {
                    return false;
                }
            }
        }

        return true;
    }

    public static void printBoard(int[][] board) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
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

        if (solveSudoku(board)) {
            printBoard(board);
            log.log(Level.INFO, "The final solution.");
        } else {
            log.log(Level.INFO, "No solution exists.");
        }
    }

}
