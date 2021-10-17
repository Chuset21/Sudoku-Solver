package com.sudoku.visual;

import com.sudoku.util.CoordinateMap;
import com.sudoku.util.SudokuGame;
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

public class SudokuApplication extends Application {
    private static final double MIN_SLIDER = 100;
    private static final double MAX_SLIDER = 200;
    private static final int MIN_WIN_SIZE = 500;
    private static final int MAX_WIN_H = 1200;
    private static final int MAX_WIN_W = 1120;

    private static final ObjectProperty<Font> FONT_TRACKING = new SimpleObjectProperty<>(Font.getDefault());

    private static final CoordinateMap<TextField> COORDINATE_MAP = new CoordinateMap<>();
    private static final PauseTransition PAUSE_VALIDATOR = new PauseTransition(Duration.seconds(1));

    private byte[][] grid;
    private SudokuGame sudokuGame;
    private boolean validate;

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
                COORDINATE_MAP.putWithCoordinates(row, col, textField);
                cell.getChildren().add(textField);

                board.add(cell, col, row);
            }
        }
        createNewGrid();

        final MenuItem easy = new MenuItem("Easy");
        setMenuItemDifficulty(easy, SudokuGame.Difficulty.EASY);

        final MenuItem medium = new MenuItem("Medium");
        setMenuItemDifficulty(medium, SudokuGame.Difficulty.MEDIUM);

        final MenuItem hard = new MenuItem("Hard");
        setMenuItemDifficulty(hard, SudokuGame.Difficulty.HARD);

        final MenuItem veryHard = new MenuItem("Very Hard");
        setMenuItemDifficulty(veryHard, SudokuGame.Difficulty.VERY_HARD);

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

    private void setMenuItemDifficulty(MenuItem menuItem, SudokuGame.Difficulty difficulty) {
        menuItem.setOnAction(event -> getNewGrid(difficulty));
    }

    private void getNewGrid(SudokuGame.Difficulty difficulty) {
        sudokuGame.generateNewGrid(difficulty);
        grid = sudokuGame.getCopyOfGrid();
        createNewGrid();
    }

    private void createNewGrid() {
        validate = false;
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
                setUpValidation(current, col, row);
            }
        }
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

    private void setUpValidation(TextField textField, byte col, byte row) {
        textField.textProperty().addListener((observable, oldValue, newValue) ->
                validate(textField, newValue, col, row));
    }

    private void validate(TextField textField, String newValue, byte col, byte row) {
        if (validate && !newValue.isEmpty()) {
            final byte val = Byte.parseByte(newValue);
            textField.setEditable(false);
            if (sudokuGame.isValueValid(val, col, row)) {
                grid[col][row] = val;
                textField.setStyle("-fx-text-fill: green; -fx-border-color: green;");
                PAUSE_VALIDATOR.setOnFinished(event -> {
                    textField.setStyle("-fx-text-fill: black;");
                    textField.setBorder(Border.EMPTY);
                });
            } else {
                textField.setStyle("-fx-text-fill: red; -fx-border-color: red;");
                PAUSE_VALIDATOR.setOnFinished(event -> {
                    textField.setStyle("-fx-text-fill: black;");
                    textField.setBorder(Border.EMPTY);
                    textField.clear();
                    textField.setEditable(true);
                });
            }
            PAUSE_VALIDATOR.play();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}