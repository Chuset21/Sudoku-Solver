package com.sudoku.visual;

import com.sudoku.util.CoordinateMap;
import com.sudoku.util.SudokuGame;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
//    private static final PseudoClass INCORRECT = PseudoClass.getPseudoClass("incorrect");

    private static final CoordinateMap<TextField> COORDINATE_MAP = new CoordinateMap<>();

    private byte[][] grid;
    private SudokuGame sudokuGame;

    @Override
    public void start(Stage primaryStage) {
        sudokuGame = new SudokuGame();
        grid = sudokuGame.getCopyOfGrid();

        final GridPane board = new GridPane();
        board.getStyleClass().add("board");

        final PseudoClass right = PseudoClass.getPseudoClass("right");
        final PseudoClass bottom = PseudoClass.getPseudoClass("bottom");

        for (byte col = 0; col < SudokuGame.GRID_BOUNDARY; col++) {
            for (byte row = 0; row < SudokuGame.GRID_BOUNDARY; row++) {
                final StackPane cell = new StackPane();
                cell.getStyleClass().add("cell");
                cell.pseudoClassStateChanged(right, col == 2 || col == 5);
                cell.pseudoClassStateChanged(bottom, row == 2 || row == 5);

                final TextField textField = createTextField();
//                setUpValidation(textField, sudokuGame, grid[col][row], col, row);
                COORDINATE_MAP.putWithCoordinates(row, col, textField);
                cell.getChildren().add(textField);

                board.add(cell, col, row);
            }
        }
        createNewGrid();

        final MenuItem easy = new MenuItem("Easy");
        easy.setOnAction(event -> {
            sudokuGame.generateNewGrid(SudokuGame.Difficulty.EASY);
            generateGrid();
        });

        final MenuItem medium = new MenuItem("Medium");
        medium.setOnAction(event -> {
            sudokuGame.generateNewGrid(SudokuGame.Difficulty.MEDIUM);
            generateGrid();
        });

        final MenuItem hard = new MenuItem("Hard");
        hard.setOnAction(event -> {
            sudokuGame.generateNewGrid(SudokuGame.Difficulty.HARD);
            generateGrid();
        });

        final MenuItem veryHard = new MenuItem("Very Hard");
        veryHard.setOnAction(event -> {
            sudokuGame.generateNewGrid(SudokuGame.Difficulty.VERY_HARD);
            generateGrid();
        });

        final MenuButton newGridButton = new MenuButton("New Puzzle", null, easy, medium, hard, veryHard);
        newGridButton.setPopupSide(Side.TOP);
        setupButton(newGridButton, board, 0);

        final Button solveButton = new Button("Solve");
        setupButton(solveButton, board, SudokuGame.GRID_BOUNDARY / 3);

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

    private void generateGrid() {
        grid = sudokuGame.getCopyOfGrid();
        createNewGrid();
    }

    private void createNewGrid() {
        for (byte col = 0; col < SudokuGame.GRID_BOUNDARY; col++) {
            for (byte row = 0; row < SudokuGame.GRID_BOUNDARY; row++) {
                final TextField current = COORDINATE_MAP.getWithCoordinates(row, col);
                final byte value = grid[col][row];

                current.setEditable(true);
                current.clear();

                if (value > 0) {
                    current.setText(String.valueOf(value));
                    current.setEditable(false);
                }
            }
        }
    }

    private void setAppSize(Stage stage) {
        stage.setMaxHeight(MAX_WIN_H);
        stage.setMaxWidth(MAX_WIN_W);
        stage.setMinHeight(MIN_WIN_SIZE);
        stage.setMinWidth(MIN_WIN_SIZE);
        stage.setHeight(MIN_WIN_SIZE);
        stage.setWidth(MIN_WIN_SIZE);
    }

    private void setupButton(ButtonBase button, GridPane board, int columnIndex) {
        button.getStyleClass().add("button");

        GridPane.setHalignment(button, HPos.CENTER);
        GridPane.setValignment(button, VPos.CENTER);
        button.setPrefSize(100, 40);

        board.add(button, columnIndex, SudokuGame.GRID_BOUNDARY, 3, 1);
    }

    private TextField createTextField() {
        final TextField textField = new TextField();

        // restrict input to integers
        textField.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("[1-9]?") ? c : null));
        textField.setAlignment(Pos.CENTER);

        textField.fontProperty().bind(FONT_TRACKING);
        textField.widthProperty().addListener((observableValue, oldWidth, newWidth) ->
                FONT_TRACKING.set(Font.font(newWidth.doubleValue() / 4)));

        return textField;
    }

//    private void setUpValidation(TextField textField, SudokuGame sudokuGame, byte n, byte col, byte row) {
//        textField.textProperty().addListener((observable, oldValue, newValue) ->
//                validate(textField, sudokuGame, n, col, row));
//        validate(textField, sudokuGame, n, col, row);
//    }
//
//    private void validate(TextField textField, SudokuGame sudokuGame, byte n, byte col, byte row) {
//        textField.pseudoClassStateChanged(INCORRECT, sudokuGame.isValueValid(n, col, row));
//    }

    public static void main(String[] args) {
        launch(args);
    }
}