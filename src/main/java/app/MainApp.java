package app;

import controlador.VentanillasController;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private VentanillasController controlador;

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/vista/View.fxml"));
        Scene scene = new Scene(loader.load(), 860, 660);
        controlador = loader.getController();

        primaryStage.setTitle("Laboratorio Concurrencia");
        primaryStage.setMinWidth(640);
        primaryStage.setMinHeight(560);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> controlador.cerrar());
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (controlador != null) {
            controlador.cerrar();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
