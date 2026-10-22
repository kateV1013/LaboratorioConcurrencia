module laboratorio.concurrencia {
    requires javafx.controls;
    requires javafx.fxml;

    opens app to javafx.fxml, javafx.graphics;
    opens controlador to javafx.fxml;
    exports app;
    exports tareas;
    exports modelo;
}
