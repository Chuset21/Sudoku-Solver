package com.sudoku.util;

import com.copy.Copy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SudokuGame {
    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD,
        VERY_HARD,
    }

    private byte[][] grid;
    private byte[][] solvedGrid;
    private Tuple<Byte> lastPosition;
    private byte solutionNum;
    private boolean isSolved;

    public static final byte GRID_BOUNDARY = 9;
    private static final List<Byte> NUMBERS = new ArrayList<>(GRID_BOUNDARY);

    static {
        for (byte number = 1; number <= GRID_BOUNDARY; number++) {
            NUMBERS.add(number);
        }
    }

    public SudokuGame(Difficulty difficulty) {
        generateNewGrid(difficulty);
        storeSolvedGrid();
    }

    /**
     * By default, the Sudoku will be set to "Medium" difficulty.
     */
    public SudokuGame() {
        this(Difficulty.MEDIUM);
    }

    public byte[][] getGrid() {
        return grid;
    }

    private boolean checkIfSolved() {
        for (byte i = 0; i < GRID_BOUNDARY; i++) {
            for (byte j = 0; j < GRID_BOUNDARY; j++) {
                if (grid[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isSolved() {
        if (!isSolved) {
            isSolved = checkIfSolved();
        } else {
            System.out.println("Solved");
        }
        return isSolved;
    }

    public byte[][] getCopyOfGrid() {
        return getCopyOfGrid(grid);
    }

    private byte[][] getCopyOfGrid(byte[][] grid) {
        return Copy.deepCopy(grid);
    }

    private Tuple<Byte> findEmpty(byte[][] grid, Tuple<Byte> lastPosition) {
        for (byte i = lastPosition.row(); i < GRID_BOUNDARY; i++) {
            for (byte j = i == lastPosition.row() ? lastPosition.col() : 0; j < GRID_BOUNDARY; j++) {
                if (grid[i][j] == 0) {
                    return new Tuple<>(i, j); // row, col
                }
            }
        }
        return null;
    }

    private boolean isValid(byte[][] grid, byte number, Tuple<Byte> position) {
        // Check row
        for (byte i = 0; i < GRID_BOUNDARY; i++) {
            if (position.col() != i && grid[position.row()][i] == number) {
                return false;
            }
        }

        // Check column
        for (byte i = 0; i < GRID_BOUNDARY; i++) {
            if (position.row() != i && grid[i][position.col()] == number) {
                return false;
            }
        }

        // Check cubes
        final int boxX = position.col() / 3;
        final int boxY = position.row() / 3;

        final int iStart = boxY * 3;
        final int iEnd = iStart + 3;
        final int jStart = boxX * 3;
        final int jEnd = jStart + 3;

        for (int i = iStart; i < iEnd; i++) {
            for (int j = jStart; j < jEnd; j++) {
                if (i != position.row() && j != position.col() && grid[i][j] == number) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean solve() {
        resetLastPosition();
        return solve(grid);
    }

    public boolean isValueValid(byte val, byte col, byte row) {
        if (solvedGrid == null) {
            storeSolvedGrid();
        }

        return solvedGrid[row][col] == val;
    }

    private void storeSolvedGrid() {
        solvedGrid = getCopyOfGrid();
        solveGrid(solvedGrid);
    }

    public byte getSolutionCell(byte row, byte col) {
        return solvedGrid[row][col];
    }

    private void solveGrid(byte[][] grid) {
        resetLastPosition();
        solve(grid);
    }

    private boolean solve(byte[][] grid) {
        // Base case: If all positions are filled up, the grid must have been solved
        final Tuple<Byte> position = findEmpty(grid, lastPosition);
        if (position == null) {
            return true;
        }

        lastPosition = position;
        for (byte num : NUMBERS) {
            if (isValid(grid, num, position)) {
                grid[position.row()][position.col()] = num;

                if (solve(grid)) {
                    return true;
                }

                // If it wasn't solved it backtracks to here
                grid[position.row()][position.col()] = 0;
                lastPosition = position;
            }
        }

        return false;
    }

    private void resetLastPosition() {
        lastPosition = new Tuple<>((byte) 0, (byte) 0);
    }

    public void generateNewGrid(Difficulty difficulty) {
        isSolved = false;
        resetLastPosition();

        // All values are automatically initialised to 0
        final byte[][] newGrid = new byte[GRID_BOUNDARY][GRID_BOUNDARY];

        // This shuffle will be used to generate the grid at random
        Collections.shuffle(NUMBERS);
        solve(newGrid);

        final byte size = GRID_BOUNDARY * GRID_BOUNDARY;
        final List<Integer> arrayPositions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            arrayPositions.add(i);
        }
        Collections.shuffle(arrayPositions);

        // We can increase/decrease the amount of iterations to increase/decrease difficulty
        final byte limit = (byte) switch (difficulty) {
            case EASY -> 30;
            case MEDIUM -> 45;
            case HARD -> 60;
            default -> size;
        };

        for (byte i = 0; i < limit; i++) {
            final int pos = arrayPositions.get(i);
            final int row = pos / GRID_BOUNDARY;
            final int col = pos % GRID_BOUNDARY;

            final byte value = newGrid[row][col];
            newGrid[row][col] = 0;

            if (!isUnique(newGrid)) {
                newGrid[row][col] = value;
            }
        }

        grid = newGrid;
    }

    private boolean isUnique(byte[][] grid) {
        resetLastPosition();
        solutionNum = 0;

        hasUniqueSolution(getCopyOfGrid(grid));

        return solutionNum == 1;
    }

    // Return value is only used to exit the recursion
    private boolean hasUniqueSolution(byte[][] grid) {
        // Base case: If all positions are filled up, the grid must have been solved
        final Tuple<Byte> position = findEmpty(grid, lastPosition);
        if (position == null) {
            solutionNum++;
            return true;
        }

        lastPosition = position;
        for (byte num : NUMBERS) {
            if (isValid(grid, num, position)) {
                grid[position.row()][position.col()] = num;

                if (hasUniqueSolution(grid) && solutionNum > 1) {
                    return true;
                }

                // If it wasn't solved it backtracks to here
                grid[position.row()][position.col()] = 0;
                lastPosition = position;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        final byte limit = GRID_BOUNDARY - 1;
        for (byte i = 0; i < limit; i++) {

            appendFirstEight(sb, i);

            final byte boardValue = grid[i][limit];
            sb.append(boardValue == 0 ? "?" : boardValue).append('\n');

            if ((i + 1) % 3 == 0) {
                sb.append("- - - - - - - - - - - - - -").append('\n');
            }
        }
        appendFirstEight(sb, limit);

        final byte boardValue = grid[limit][limit];
        return sb.append(boardValue == 0 ? "?" : boardValue).toString();
    }

    private void appendFirstEight(StringBuilder sb, byte row) {
        for (byte j = 0; j < 8; j++) {
            final byte boardValue = grid[row][j];
            sb.append(boardValue == 0 ? "?" : boardValue).append((j + 1) % 3 == 0 ? " | " : "  ");
        }
    }

    public static void main(String[] args) {
        final SudokuGame sudokuGame = new SudokuGame();
        System.out.println(sudokuGame);
        System.out.println();

        // Testing that the copy is deep (not shallow)
        final byte[][] copy = sudokuGame.getCopyOfGrid();
        copy[0][0] = 78;

        System.out.println(sudokuGame.grid[0][0] == 78);
        System.out.println();

        // Testing the solve method
        if (sudokuGame.solve()) {
            System.out.println(sudokuGame);
        } else {
            System.out.println("No possible solution???");
        }

        // Testing the isUnique method (should return false here)
        System.out.println();
        System.out.println(sudokuGame.isUnique(new byte[][]{
                {2, 9, 5, 7, 4, 3, 8, 6, 1},
                {4, 3, 1, 8, 6, 5, 9, 0, 0},
                {8, 7, 6, 1, 9, 2, 5, 4, 3},
                {3, 8, 7, 4, 5, 9, 2, 1, 6},
                {6, 1, 2, 3, 8, 7, 4, 9, 5},
                {5, 4, 9, 2, 1, 6, 7, 3, 8},
                {7, 6, 3, 5, 3, 4, 1, 8, 9},
                {9, 2, 8, 6, 7, 1, 3, 5, 4},
                {1, 5, 4, 9, 3, 8, 6, 0, 0}
        }));

        // Testing the random generation
        sudokuGame.generateNewGrid(Difficulty.VERY_HARD);
        System.out.println();
        System.out.println(sudokuGame);

        // Testing if the generated sudoku has a unique solution
        System.out.println();
        System.out.println(sudokuGame.isUnique(sudokuGame.grid));
    }
}
