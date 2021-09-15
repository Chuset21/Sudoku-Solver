module com.sudoku.sudokusolver {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.sudoku.sudokusolver to javafx.fxml;
    exports com.sudoku.sudokusolver;
}