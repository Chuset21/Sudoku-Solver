package com.sudoku.visual;

import com.sudoku.util.SudokuGame;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class SudokuApplication extends Application {
    private static final double MIN_SLIDER = 100;
    private static final double MAX_SLIDER = 200;
    private static final int MIN_WIN_SIZE = 500;
    private static final int MAX_WIN_H = 1200;
    private static final int MAX_WIN_W = 1120;
    private static final ObjectProperty<Font> FONT_TRACKING = new SimpleObjectProperty<>(Font.getDefault());

    @Override
    public void start(Stage primaryStage) {
        final SudokuGame sudokuGame = new SudokuGame();
        sudokuGame.storeSolvedGrid();
        final byte[][] grid = sudokuGame.getCopyOfGrid();

        final GridPane board = new GridPane();
        board.getStyleClass().add("board");

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

        final Button newGridButton = new Button("New Puzzle");
        setupButton(newGridButton, board, 0);

        final Button solveButton = new Button("Solve");
        setupButton(solveButton, board,SudokuGame.GRID_BOUNDARY / 3);

        final Button hintButton = new Button("Get Hint");
        setupButton(hintButton, board, 2 * (SudokuGame.GRID_BOUNDARY / 3));

        final Slider slider = new Slider(MIN_SLIDER, MAX_SLIDER, MIN_SLIDER + (MAX_SLIDER - MIN_SLIDER) / 2);
        slider.getStyleClass().add("slider");
        GridPane.setHalignment(slider, HPos.CENTER);
        GridPane.setValignment(slider, VPos.CENTER);
        board.add(slider, SudokuGame.GRID_BOUNDARY / 3, SudokuGame.GRID_BOUNDARY + 1, 3, 1);

        final Scene scene = new Scene(board);
        scene.getStylesheets().add("sudoku.css");

        board.setAlignment(Pos.CENTER);
        board.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        setAppSize(primaryStage);

        primaryStage.setTitle("Sudoku Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setAppSize(Stage stage) {
        stage.setMaxHeight(MAX_WIN_H);
        stage.setMaxWidth(MAX_WIN_W);
        stage.setMinHeight(MIN_WIN_SIZE);
        stage.setMinWidth(MIN_WIN_SIZE);
        stage.setHeight(MIN_WIN_SIZE);
        stage.setWidth(MIN_WIN_SIZE);
    }

    private void setupButton(Button button, GridPane board, int columnIndex) {
        button.getStyleClass().add("button");

        GridPane.setHalignment(button, HPos.CENTER);
        GridPane.setValignment(button, VPos.CENTER);
        button.setPrefSize(100, 40);

        board.add(button, columnIndex, SudokuGame.GRID_BOUNDARY, 3, 1);
    }

    private TextField createTextField(byte n) {
        final TextField textField = new TextField();

        // restrict input to integers
        textField.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("[1-9]?") ? c : null));
        textField.setAlignment(Pos.CENTER);

        textField.fontProperty().bind(FONT_TRACKING);
        textField.widthProperty().addListener((observableValue, oldWidth, newWidth) ->
                FONT_TRACKING.set(Font.font(newWidth.doubleValue() / 4)));

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