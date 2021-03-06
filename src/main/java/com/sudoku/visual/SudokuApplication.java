package com.sudoku.visual;

import com.sudoku.util.CoordinateMap;
import com.sudoku.util.SudokuGame;
import com.sudoku.util.Tuple;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
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

import java.util.Collections;
import java.util.Stack;

import static com.sudoku.util.SudokuGame.GRID_BOUNDARY;

public class SudokuApplication extends Application {
    private static final long MIN_SLIDER = 200;
    private static final long MAX_SLIDER = 1500;
    private static final int MIN_WIN_SIZE = 500;
    private static final int MAX_WIN_H = 1200;
    private static final int MAX_WIN_W = 1120;

    private static final ObjectProperty<Font> FONT_TRACKING = new SimpleObjectProperty<>(Font.getDefault());
    private static final CoordinateMap<TextField> COORDINATE_MAP = new CoordinateMap<>();

    private static final byte[] NUMBERS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    private static final byte PAUSE_DURATION = 1;
    private static final PauseTransition HINT_PAUSE = new PauseTransition(Duration.seconds(PAUSE_DURATION));

    private boolean isCurrentHard = false;
    private long visualPauseDur = (MAX_SLIDER - MIN_SLIDER) / 2;
    private boolean isBeingSolved = false;
    private Tuple<Byte> lastPosition;
    private final Stack<Tuple<Byte>> emptyCellList = new Stack<>();
    private final SudokuGame sudokuGame = new SudokuGame();
    private byte[][] grid;
    private boolean validate;
    private MenuButton newGridButton;
    private Button solveButton;
    private Button hintButton;

    @Override
    public void start(Stage primaryStage) {
        grid = sudokuGame.getGrid();

        final GridPane board = new GridPane();
        board.getStyleClass().add("board");

        final PseudoClass right = PseudoClass.getPseudoClass("right");
        final PseudoClass bottom = PseudoClass.getPseudoClass("bottom");

        for (byte col = 0; col < GRID_BOUNDARY; col++) {
            for (byte row = 0; row < GRID_BOUNDARY; row++) {
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

        newGridButton = new MenuButton("New Puzzle", null, easy, medium, hard, veryHard);
        newGridButton.setPopupSide(Side.TOP);
        setupButton(newGridButton, board, 0);

        solveButton = new Button("Solve");
        setupButton(solveButton, board, GRID_BOUNDARY / 3);
        solveButton.setOnAction(event -> solve());

        hintButton = new Button("Get Hint");
        setupButton(hintButton, board, 2 * (GRID_BOUNDARY / 3));
        setupHintAction();

        final Slider slider = new Slider(MIN_SLIDER, MAX_SLIDER, MIN_SLIDER + (double) (MAX_SLIDER - MIN_SLIDER) / 2);
        slider.getStyleClass().add("slider");
        GridPane.setHalignment(slider, HPos.CENTER);
        GridPane.setValignment(slider, VPos.CENTER);
        board.add(slider, GRID_BOUNDARY / 3, GRID_BOUNDARY + 1, 3, 1);
        slider.valueProperty().addListener((observable, oldValue, newValue) ->
                visualPauseDur = invertRange(newValue.longValue()));

        setMenuItemDifficulty(easy, SudokuGame.Difficulty.EASY);
        setMenuItemDifficulty(medium, SudokuGame.Difficulty.MEDIUM);
        setMenuItemDifficulty(hard, SudokuGame.Difficulty.HARD);
        setMenuItemDifficulty(veryHard, SudokuGame.Difficulty.VERY_HARD);

        createNewGrid();

        final Scene scene = new Scene(board);
        scene.getStylesheets().add("sudoku.css");

        board.setAlignment(Pos.CENTER);
        board.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        setAppSize(primaryStage);

        primaryStage.setTitle("Sudoku Game");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    private void setDisableButtons(boolean disable) {
        newGridButton.setDisable(disable);
        solveButton.setDisable(disable);
        hintButton.setDisable(disable);
    }

    private void resetLastPosition() {
        lastPosition = new Tuple<>((byte) 0, (byte) 0);
    }

    private void playOnSolve() {
        for (byte col = 0; col < GRID_BOUNDARY; col++) {
            for (byte row = 0; row < GRID_BOUNDARY; row++) {
                COORDINATE_MAP.getWithCoordinates(row, col).
                        setStyle("-fx-text-fill: green; -fx-border-color: green;");
            }
        }

        final PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> {
            for (byte col = 0; col < GRID_BOUNDARY; col++) {
                for (byte row = 0; row < GRID_BOUNDARY; row++) {
                    final TextField textField = COORDINATE_MAP.getWithCoordinates(row, col);
                    textField.setStyle("-fx-text-fill: black;");
                    textField.setBorder(Border.EMPTY);
                }
            }
            newGridButton.setDisable(false);
            solveButton.setDisable(true);
            hintButton.setDisable(true);
            isBeingSolved = false;
        });
        pause.play();
    }

    private void solve() {
        isBeingSolved = true;
        resetLastPosition();
        setDisableButtons(true);
        emptyCellList.forEach(t -> COORDINATE_MAP.getWithCoordinates(t.row(), t.col()).setEditable(false));

        // Threads can cause exceptions and, to be completely honest, they are beyond me at this point
        new Thread(() -> {
            solveGrid();
            playOnSolve();
        }).start();
    }

    private boolean solveGrid() {
        final Tuple<Byte> position = sudokuGame.findEmpty(grid, lastPosition);
        if (position == null) {
            return true;
        }

        lastPosition = position;
        final TextField currentCell = COORDINATE_MAP.getWithCoordinates(lastPosition.row(), lastPosition.col());
        for (byte i = 0; i < GRID_BOUNDARY; i++) {
            /*
             If it's hard or very hard it will basically cheat by using an optimised order of numbers [1-9]
             that was created when generating the sudoku grid.
             This is only done so that the user isn't forced to close the application when the solving
             algorithm starts to be painfully slow.
             */
            final byte num = isCurrentHard ? SudokuGame.NUMBERS.get(i) : NUMBERS[i];
            if (sudokuGame.isValid(grid, num, position)) {
                grid[position.row()][position.col()] = num;

                currentCell.setText(String.valueOf(num));
                if (sudokuGame.isValueValid(num, position.col(), position.row())) {
                    currentCell.setStyle("-fx-text-fill: green; -fx-border-color: green;");
                } else {
                    currentCell.setStyle("-fx-text-fill: red; -fx-border-color: red;");
                }

                try {
                    Thread.sleep(visualPauseDur);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (solveGrid()) {
                    return true;
                }

                // If it wasn't solved it backtracks to here
                currentCell.clear();
                currentCell.setStyle("-fx-text-fill: black;");
                currentCell.setBorder(Border.EMPTY);
                grid[position.row()][position.col()] = 0;
                lastPosition = position;
            } else {
                currentCell.setText(String.valueOf(num));

                try {
                    Thread.sleep(visualPauseDur / 5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                currentCell.clear();
            }
        }

        return false;
    }

    private long invertRange(long x) {
        return MAX_SLIDER - x + MIN_SLIDER;
    }

    private void setMenuItemDifficulty(MenuItem menuItem, SudokuGame.Difficulty difficulty) {
        menuItem.setOnAction(event -> {
            isCurrentHard = SudokuGame.Difficulty.VERY_HARD == difficulty || SudokuGame.Difficulty.HARD == difficulty;
            getNewGrid(difficulty);
        });
    }

    private void getNewGrid(SudokuGame.Difficulty difficulty) {
        sudokuGame.generateNewGrid(difficulty);
        grid = sudokuGame.getGrid();
        createNewGrid();
    }

    private void createNewGrid() {
        setDisableButtons(false);

        validate = false;
        emptyCellList.clear();
        for (byte col = 0; col < GRID_BOUNDARY; col++) {
            for (byte row = 0; row < GRID_BOUNDARY; row++) {
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

                setUpValidation(current, col, row);
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

    private void setupHintAction() {
        HINT_PAUSE.setOnFinished(event -> {
            setDisableButtons(false);
            emptyCellList.forEach(t -> COORDINATE_MAP.getWithCoordinates(t.row(), t.col()).setEditable(true));
        });

        hintButton.setOnAction(event -> {
            if (!sudokuGame.isSolved()) {
                final Tuple<Byte> current = emptyCellList.pop();

                final byte value = sudokuGame.getSolutionCell(current.row(), current.col());
                grid[current.row()][current.col()] = value;
                COORDINATE_MAP.getWithCoordinates(current.row(), current.col()).setText(String.valueOf(value));

                setDisableButtons(true);
                emptyCellList.forEach(t -> COORDINATE_MAP.getWithCoordinates(t.row(), t.col()).setEditable(false));
                if (sudokuGame.isSolved()) {
                    playOnSolve();
                } else {
                    HINT_PAUSE.play();
                }
            } else {
                playOnSolve();
            }
        });
    }

    private void setupButton(ButtonBase button, GridPane board, int columnIndex) {
        button.getStyleClass().add("button");

        GridPane.setHalignment(button, HPos.CENTER);
        GridPane.setValignment(button, VPos.CENTER);
        button.setPrefSize(100, 40);

        board.add(button, columnIndex, GRID_BOUNDARY, 3, 1);
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
        if (!isBeingSolved && validate && !newValue.isEmpty()) {
            final PauseTransition pause = new PauseTransition(Duration.seconds(PAUSE_DURATION));

            setDisableButtons(true);
            emptyCellList.forEach(t -> COORDINATE_MAP.getWithCoordinates(t.row(), t.col()).setEditable(false));

            final byte val = Byte.parseByte(newValue);
            textField.setEditable(false);
            if (sudokuGame.isValueValid(val, col, row)) {
                grid[row][col] = val;
                emptyCellList.remove(new Tuple<>(row, col));
                if (sudokuGame.isSolved()) {
                    pause.setDuration(Duration.ZERO);
                    playOnSolve();
                } else {
                    textField.setStyle("-fx-text-fill: green; -fx-border-color: green;");
                    pause.setOnFinished(event -> {
                        textField.setStyle("-fx-text-fill: black;");
                        textField.setBorder(Border.EMPTY);
                        emptyCellList.forEach(t -> COORDINATE_MAP.getWithCoordinates(t.row(), t.col()).setEditable(true));
                        setDisableButtons(false);
                    });
                }
            } else {
                textField.setStyle("-fx-text-fill: red; -fx-border-color: red;");
                pause.setOnFinished(event -> {
                    textField.setStyle("-fx-text-fill: black;");
                    textField.setBorder(Border.EMPTY);
                    textField.clear();

                    textField.setEditable(true);
                    emptyCellList.forEach(t -> COORDINATE_MAP.getWithCoordinates(t.row(), t.col()).setEditable(true));
                    setDisableButtons(false);
                });
            }
            pause.play();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}