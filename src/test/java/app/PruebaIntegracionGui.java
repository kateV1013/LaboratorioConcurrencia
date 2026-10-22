package app;

import controlador.VentanillasController;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/** Prueba ejecutable sin dependencias de pruebas adicionales ni ventanas visibles. */
public class PruebaIntegracionGui {

    private static Parent vista;
    private static Button botonIniciar;
    private static Button botonDetener;
    private static VentanillasController controlador;
    private static final AtomicBoolean cambioFueraDeFx = new AtomicBoolean();

    public static void main(String[] args) throws Exception {
        CountDownLatch inicio = new CountDownLatch(1);
        Platform.startup(inicio::countDown);
        verificar(inicio.await(5, TimeUnit.SECONDS), "JavaFX no inició");
        try {
            enFx(() -> {
                FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/vista/View.fxml"));
                vista = loader.load();
                controlador = loader.getController();
                Scene escena = new Scene(vista, 860, 660);
                vista.applyCss();
                vista.layout();
                botonIniciar = (Button) vista.lookup("#iniciar");
                botonDetener = (Button) vista.lookup("#detener");
                for (String selector : List.of("#contador", "#estado")) {
                    ((Label) vista.lookup(selector)).textProperty().addListener((obs, antes, ahora) -> {
                        if (!Platform.isFxApplicationThread()) {
                            cambioFueraDeFx.set(true);
                        }
                    });
                }
                ((TextArea) vista.lookup("#logs")).textProperty().addListener((obs, antes, ahora) -> {
                    if (!Platform.isFxApplicationThread()) {
                        cambioFueraDeFx.set(true);
                    }
                });
                verificar(estado().equals("En espera"), "Estado inicial incorrecto");
                verificar(!botonIniciar.isDisabled(), "Iniciar debe estar habilitado");
                verificar(botonDetener.isDisabled(), "Detener debe estar deshabilitado");
                botonIniciar.fire();
                botonIniciar.fire();
                verificar(estado().equals("Ejecutando"), "No inició la simulación");
                verificar(botonIniciar.isDisabled(), "Iniciar debe bloquearse al ejecutar");
                verificar(!botonDetener.isDisabled(), "Detener debe habilitarse");
                return null;
            });

            List<Thread> primeraEjecucion = ventanillasActivas();
            verificar(primeraEjecucion.size() == 2, "Un doble clic creó hilos extra");
            esperarTexto("#contador", texto -> Integer.parseInt(texto) > 0);
            enFx(() -> {
                botonDetener.fire();
                verificar(estado().equals("Deteniendo"), "No se mostró Deteniendo");
                verificar(botonIniciar.isDisabled(), "Iniciar se habilitó antes de terminar");
                verificar(botonDetener.isDisabled(), "Detener debe bloquearse al cancelar");
                botonIniciar.fire();
                botonDetener.fire();
                return null;
            });
            esperarTexto("#estado", "Detenido"::equals);
            verificar(primeraEjecucion.stream().noneMatch(Thread::isAlive),
                    "Se habilitó el reinicio antes de terminar los hilos previos");

            enFx(() -> {
                verificar(!botonIniciar.isDisabled(), "No se habilitó el reinicio");
                botonIniciar.fire();
                verificar(((Label) vista.lookup("#contador")).getText().equals("0"),
                        "El contador no se reinició");
                return null;
            });
            verificar(ventanillasActivas().stream().noneMatch(primeraEjecucion::contains),
                    "Se reutilizaron hilos terminados");
            esperarTexto("#estado", "Finalizado"::equals);
            enFx(() -> {
                verificar(((Label) vista.lookup("#contador")).getText().equals("10"),
                        "El contador final debe ser 10");
                verificar(!botonIniciar.isDisabled(), "Iniciar debe habilitarse al finalizar");
                verificar(botonDetener.isDisabled(), "Detener debe deshabilitarse al finalizar");
                String logs = ((TextArea) vista.lookup("#logs")).getText();
                verificar(logs.contains("Ventanilla-A") && logs.contains("Ventanilla-B")
                        && logs.contains("Finalizado"), "Faltan eventos en los logs");
                return null;
            });

            for (int i = 0; i < 3; i++) {
                enFx(() -> {
                    botonIniciar.fire();
                    botonDetener.fire();
                    verificar(estado().equals("Deteniendo"), "Falló la detención inmediata");
                    verificar(botonIniciar.isDisabled(), "Se permitió un inicio mientras se detenía");
                    return null;
                });
                esperarTexto("#estado", "Detenido"::equals);
                verificar(ventanillasActivas().isEmpty(), "Quedaron ventanillas de la ejecución previa");
            }

            enFx(() -> { botonIniciar.fire(); return null; });
            List<Thread> alCerrar = ventanillasActivas();
            enFx(() -> { controlador.cerrar(); return null; });
            for (Thread hilo : alCerrar) {
                hilo.join(2000); // Este main NO es el hilo de JavaFX.
                verificar(!hilo.isAlive(), "Quedó un hilo vivo al cerrar");
            }
            verificar(!cambioFueraDeFx.get(), "Se modificó la interfaz fuera del hilo de JavaFX");
            System.out.println("OK: FXML, estados, botones, contador, logs, reinicios, detención inmediata y cierre.");
        } finally {
            try {
                enFx(() -> {
                    if (controlador != null) {
                        controlador.cerrar();
                    }
                    return null;
                });
            } finally {
                Platform.exit();
            }
        }
    }

    private static String estado() {
        return ((Label) vista.lookup("#estado")).getText();
    }

    private static List<Thread> ventanillasActivas() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isAlive)
                .filter(hilo -> hilo.getName().equals("Ventanilla-A")
                        || hilo.getName().equals("Ventanilla-B"))
                .toList();
    }

    private static void esperarTexto(String selector, Predicate<String> esperado) throws Exception {
        CountDownLatch cambio = new CountDownLatch(1);
        enFx(() -> {
            Label etiqueta = (Label) vista.lookup(selector);
            if (esperado.test(etiqueta.getText())) {
                cambio.countDown();
            } else {
                etiqueta.textProperty().addListener((observable, antes, ahora) -> {
                    if (esperado.test(ahora)) {
                        cambio.countDown();
                    }
                });
            }
            return null;
        });
        verificar(cambio.await(8, TimeUnit.SECONDS), "No llegó la actualización de " + selector);
    }

    private static <T> T enFx(Callable<T> accion) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return tarea.get(5, TimeUnit.SECONDS);
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }
}
