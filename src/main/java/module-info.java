module com.sudoku.visual {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.sudoku.visual to javafx.fxml;
    exports com.sudoku.visual;
}