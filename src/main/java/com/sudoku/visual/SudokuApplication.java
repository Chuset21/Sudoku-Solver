package com.sudoku.visual;

import javafx.application.Application;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SudokuApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        final GridPane board = new GridPane();

        final PseudoClass right = PseudoClass.getPseudoClass("right");
        final PseudoClass bottom = PseudoClass.getPseudoClass("bottom");

        for (int col = 0; col < 9; col++) {
            for (int row = 0; row < 9; row++) {
                final StackPane cell = new StackPane();
                cell.getStyleClass().add("cell");
                cell.pseudoClassStateChanged(right, col == 2 || col == 5);
                cell.pseudoClassStateChanged(bottom, row == 2 || row == 5);

                cell.getChildren().add(createTextField());

                board.add(cell, col, row);
            }
        }


        final Scene scene = new Scene(board);
        scene.getStylesheets().add("sudoku.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TextField createTextField() {
        final TextField textField = new TextField();

        // restrict input to integers:
        textField.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d?") ? c : null));
        return textField;
    }

    public static void main(String[] args) {
        launch(args);
    }
}