module laboratorio.concurrencia {
    requires javafx.controls;
    requires javafx.fxml;

    opens app to javafx.fxml, javafx.graphics;
    exports app;
    exports tareas;
    exports modelo;
}
