package com.sudoku.visual;

import com.sudoku.util.SudokuGame;
import javafx.application.Application;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SudokuApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        final SudokuGame sudokuGame = new SudokuGame();
        sudokuGame.storeSolvedGrid();
        final byte[][] grid = sudokuGame.getCopyOfGrid();

        final GridPane board = new GridPane();

        final PseudoClass right = PseudoClass.getPseudoClass("right");
        final PseudoClass bottom = PseudoClass.getPseudoClass("bottom");

        for (int col = 0; col < SudokuGame.GRID_BOUNDARY; col++) {
            for (int row = 0; row < SudokuGame.GRID_BOUNDARY; row++) {
                final StackPane cell = new StackPane();
                cell.getStyleClass().add("cell");
                cell.pseudoClassStateChanged(right, col == 2 || col == 5);
                cell.pseudoClassStateChanged(bottom, row == 2 || row == 5);

                cell.getChildren().add(createTextField(grid[col][row]));

                board.add(cell, col, row);
            }
        }

        final Button solveButton = new Button("Solve");
        setupButton(solveButton, board,SudokuGame.GRID_BOUNDARY / 3);

        final Button newGridButton = new Button("New Puzzle");
        setupButton(newGridButton, board, 0);

        final Button hintButton = new Button("Get Hint");
        setupButton(hintButton, board, 2 * (SudokuGame.GRID_BOUNDARY / 3));

        final Scene scene = new Scene(board);
        scene.getStylesheets().add("sudoku.css");

        board.setAlignment(Pos.CENTER);
        board.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        primaryStage.setMaxHeight(Double.MAX_VALUE);
        primaryStage.setMaxWidth(Double.MAX_VALUE);
        primaryStage.setMinHeight(500);
        primaryStage.setMinWidth(500);

        primaryStage.setTitle("Sudoku Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupButton(Button button, GridPane board, int columnIndex) {
        button.getStyleClass().add("button");

        GridPane.setHalignment(button, HPos.CENTER);
        GridPane.setValignment(button, VPos.CENTER);
        button.setMinSize(100, 40);

        board.add(button, columnIndex, SudokuGame.GRID_BOUNDARY, 3, 1);
    }

    private TextField createTextField(int n) {
        final TextField textField = new TextField();

        // restrict input to integers:
        textField.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d?") ? c : null));
        textField.setAlignment(Pos.CENTER);

        if (n > 0) {
            textField.setText(String.valueOf(n));
            textField.setEditable(false);
        }

        return textField;
    }

    public static void main(String[] args) {
        launch(args);
    }
}