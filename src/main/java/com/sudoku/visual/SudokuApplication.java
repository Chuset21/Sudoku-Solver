package com.sudoku.visual;

import com.sudoku.util.SudokuGame;
import javafx.application.Application;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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

        final Scene scene = new Scene(board);
        scene.getStylesheets().add("sudoku.css");
        primaryStage.setScene(scene);
        primaryStage.show();
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