package com.sudoku.visual;

import com.sudoku.util.CoordinateMap;
import com.sudoku.util.SudokuGame;
import com.sudoku.util.Tuple;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

public class SudokuApplication extends Application {
    private static final double MIN_SLIDER = 0.4;
    private static final double MAX_SLIDER = 2;
    private static final int MIN_WIN_SIZE = 500;
    private static final int MAX_WIN_H = 1200;
    private static final int MAX_WIN_W = 1120;

    private static final ObjectProperty<Font> FONT_TRACKING = new SimpleObjectProperty<>(Font.getDefault());
    private static final CoordinateMap<TextField> COORDINATE_MAP = new CoordinateMap<>();

    private static final char PAUSE_DURATION = 1;
    private static final PauseTransition HINT_PAUSE = new PauseTransition(Duration.seconds(PAUSE_DURATION));

    private double visualPauseDur = 1;
    private boolean isBeingSolved = false;
    private final Stack<Tuple<Byte>> emptyCellList = new Stack<>();
    private final SudokuGame sudokuGame = new SudokuGame();
    private byte[][] grid;
    private boolean validate;

    @Override
    public void start(Stage primaryStage) {
        grid = sudokuGame.getGrid();

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
                COORDINATE_MAP.putWithCoordinates(row, col, textField);
                cell.getChildren().add(textField);

                board.add(cell, col, row);
            }
        }

        final MenuItem easy = new MenuItem("Easy");
        final MenuItem medium = new MenuItem("Medium");
        final MenuItem hard = new MenuItem("Hard");
        final MenuItem veryHard = new MenuItem("Very Hard");

        final MenuButton newGridButton = new MenuButton("New Puzzle", null, easy, medium, hard, veryHard);
        newGridButton.setPopupSide(Side.TOP);
        setupButton(newGridButton, board, 0);

        final Button solveButton = new Button("Solve");
        setupButton(solveButton, board, SudokuGame.GRID_BOUNDARY / 3);

        final Button hintButton = new Button("Get Hint");
        setupButton(hintButton, board, 2 * (SudokuGame.GRID_BOUNDARY / 3));
        setupHintAction(hintButton, solveButton, newGridButton);

        final Slider slider = new Slider(MIN_SLIDER, MAX_SLIDER, MIN_SLIDER + (MAX_SLIDER - MIN_SLIDER) / 2);
        slider.getStyleClass().add("slider");
        GridPane.setHalignment(slider, HPos.CENTER);
        GridPane.setValignment(slider, VPos.CENTER);
        board.add(slider, SudokuGame.GRID_BOUNDARY / 3, SudokuGame.GRID_BOUNDARY + 1, 3, 1);
        slider.valueProperty().addListener((observable, oldValue, newValue) ->
                visualPauseDur = invertRange(newValue.doubleValue()));

        setMenuItemDifficulty(easy, SudokuGame.Difficulty.EASY, solveButton, hintButton);
        setMenuItemDifficulty(medium, SudokuGame.Difficulty.MEDIUM, solveButton, hintButton);
        setMenuItemDifficulty(hard, SudokuGame.Difficulty.HARD, solveButton, hintButton);
        setMenuItemDifficulty(veryHard, SudokuGame.Difficulty.VERY_HARD, solveButton, hintButton);

        createNewGrid(solveButton, hintButton, solveButton);

        final Scene scene = new Scene(board);
        scene.getStylesheets().add("sudoku.css");

        board.setAlignment(Pos.CENTER);
        board.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        setAppSize(primaryStage);

        primaryStage.setTitle("Sudoku Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private double invertRange(double x) {
        return MAX_SLIDER - x + MIN_SLIDER;
    }

    private void setMenuItemDifficulty(MenuItem menuItem, SudokuGame.Difficulty difficulty, Button... buttons) {
        menuItem.setOnAction(event -> getNewGrid(difficulty, buttons));
    }

    private void getNewGrid(SudokuGame.Difficulty difficulty, Button... buttons) {
        sudokuGame.generateNewGrid(difficulty);
        grid = sudokuGame.getGrid();
        createNewGrid(buttons);
    }

    private void createNewGrid(Button... buttons) {
        Arrays.stream(buttons).forEach(b -> b.setDisable(false));

        validate = false;
        emptyCellList.clear();
        for (byte col = 0; col < SudokuGame.GRID_BOUNDARY; col++) {
            for (byte row = 0; row < SudokuGame.GRID_BOUNDARY; row++) {
                final TextField current = COORDINATE_MAP.getWithCoordinates(row, col);
                final byte value = grid[row][col];

                if (value > 0) {
                    current.setText(String.valueOf(value));
                    current.setEditable(false);
                } else {
                    current.setEditable(true);
                    current.clear();
                    emptyCellList.add(new Tuple<>(row, col));
                }

                setUpValidation(current, col, row, buttons);
            }
        }
        Collections.shuffle(emptyCellList);
        validate = true;
    }

    private void setAppSize(Stage stage) {
        stage.setMaxHeight(MAX_WIN_H);
        stage.setMaxWidth(MAX_WIN_W);
        stage.setMinHeight(MIN_WIN_SIZE);
        stage.setMinWidth(MIN_WIN_SIZE);
        stage.setHeight(MIN_WIN_SIZE);
        stage.setWidth(MIN_WIN_SIZE);
    }

    private void setupHintAction(Button hintButton, Button solveButton, MenuButton newGridButton) {
        HINT_PAUSE.setOnFinished(event -> {
            hintButton.setDisable(false);
            solveButton.setDisable(false);
            newGridButton.setDisable(false);
            emptyCellList.forEach(t -> COORDINATE_MAP.getWithCoordinates(t.row(), t.col()).setEditable(true));
        });

        hintButton.setOnAction(event -> {
            if (!sudokuGame.isSolved()) {
                final Tuple<Byte> current = emptyCellList.pop();

                final byte value = sudokuGame.getSolutionCell(current.row(), current.col());
                grid[current.row()][current.col()] = value;
                COORDINATE_MAP.getWithCoordinates(current.row(), current.col()).setText(String.valueOf(value));

                hintButton.setDisable(true);
                solveButton.setDisable(true);
                newGridButton.setDisable(true);
                emptyCellList.forEach(t -> COORDINATE_MAP.getWithCoordinates(t.row(), t.col()).setEditable(false));
                HINT_PAUSE.play();
            } else {
                hintButton.setDisable(true);
                solveButton.setDisable(true);
            }
        });
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

    private void setUpValidation(TextField textField, byte col, byte row, Button... buttons) {
        textField.textProperty().addListener((observable, oldValue, newValue) ->
                validate(textField, newValue, col, row, buttons));
    }

    private void validate(TextField textField, String newValue, byte col, byte row, Button... buttons) {
        if (validate && !newValue.isEmpty()) {
            final PauseTransition pause = new PauseTransition(Duration.seconds(isBeingSolved ?
                    visualPauseDur : PAUSE_DURATION));

            Arrays.stream(buttons).forEach(b -> b.setDisable(true));
            emptyCellList.forEach(t -> COORDINATE_MAP.getWithCoordinates(t.row(), t.col()).setEditable(false));

            final byte val = Byte.parseByte(newValue);
            textField.setEditable(false);
            if (sudokuGame.isValueValid(val, col, row)) {
                grid[row][col] = val;
                emptyCellList.remove(new Tuple<>(row, col));
                textField.setStyle("-fx-text-fill: green; -fx-border-color: green;");
                pause.setOnFinished(event -> {
                    textField.setStyle("-fx-text-fill: black;");
                    textField.setBorder(Border.EMPTY);
                    emptyCellList.forEach(t -> COORDINATE_MAP.getWithCoordinates(t.row(), t.col()).setEditable(!isBeingSolved));
                    if (sudokuGame.isSolved()) {
                        Arrays.stream(buttons).forEach(b -> b.setDisable(true));
                    } else if (!isBeingSolved) {
                        Arrays.stream(buttons).forEach(b -> b.setDisable(false));
                    }
                });
            } else {
                textField.setStyle("-fx-text-fill: red; -fx-border-color: red;");
                pause.setOnFinished(event -> {
                    textField.setStyle("-fx-text-fill: black;");
                    textField.setBorder(Border.EMPTY);
                    textField.clear();
                    if (!isBeingSolved) {
                        textField.setEditable(true);
                        emptyCellList.forEach(t -> COORDINATE_MAP.getWithCoordinates(t.row(), t.col()).setEditable(true));
                        Arrays.stream(buttons).forEach(b -> b.setDisable(false));
                    }
                });
            }
            pause.play();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}