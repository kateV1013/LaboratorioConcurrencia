package controlador;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import modelo.ContadorCompartido;
import tareas.ProcesoVentanilla;

public class VentanillasController {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int TOTAL_TURNOS = 2 * ProcesoVentanilla.TURNOS_POR_VENTANILLA;

    @FXML private Button botonIniciar;
    @FXML private Button botonDetener;
    @FXML private Label estado;
    @FXML private Label contadorLabel;
    @FXML private Label indicacion;
    @FXML private ProgressBar progreso;
    @FXML private TextArea logs;

    private final ContadorCompartido contador = new ContadorCompartido();
    private final List<ProcesoVentanilla> procesos = new ArrayList<>();
    private final List<Thread> hilos = new ArrayList<>();

    // Estos campos se consultan y modifican únicamente en el hilo de JavaFX.
    private boolean enEjecucion;
    private boolean detencionSolicitada;
    private boolean huboError;
    private boolean cerrado;
    private int numeroEjecucion;

    @FXML
    private void initialize() {
        agregarLog("Ventanillas listas.");
    }

    @FXML
    private void iniciar() {
        if (cerrado || enEjecucion || hilos.stream().anyMatch(Thread::isAlive)) {
            return;
        }

        contador.reiniciar();
        procesos.clear();
        hilos.clear();
        detencionSolicitada = false;
        huboError = false;
        enEjecucion = true;
        numeroEjecucion++;
        actualizarContador(0);
        actualizarEstado("Ejecutando", true, false);
        agregarLog("Ejecución " + numeroEjecucion + " iniciada. Contador reiniciado.");

        for (String nombre : List.of("Ventanilla-A", "Ventanilla-B")) {
            ProcesoVentanilla proceso = new ProcesoVentanilla(nombre, contador, this::registrarEvento);
            Thread hilo = new Thread(proceso, nombre);
            hilo.setUncaughtExceptionHandler((origen, error) -> Platform.runLater(() -> {
                if (!cerrado) {
                    huboError = true;
                    agregarLog("Error en " + origen.getName() + ": " + error);
                    detener();
                }
            }));
            procesos.add(proceso);
            hilos.add(hilo);
        }
        hilos.forEach(Thread::start);

        // join() se ejecuta en un coordinador, nunca en el hilo de JavaFX.
        List<Thread> hilosActuales = List.copyOf(hilos);
        Thread coordinador = new Thread(() -> esperarFinalizacion(hilosActuales),
                "Coordinador-" + numeroEjecucion);
        coordinador.setDaemon(true);
        coordinador.start();
    }

    @FXML
    private void detener() {
        if (cerrado || !enEjecucion || detencionSolicitada) {
            return;
        }
        detencionSolicitada = true;
        actualizarEstado("Deteniendo", true, true);
        agregarLog("Detención solicitada. Esperando la terminación de las ventanillas...");
        solicitarDetencion();
    }

    private void solicitarDetencion() {
        procesos.forEach(ProcesoVentanilla::detener);
        hilos.forEach(Thread::interrupt);
    }

    private void registrarEvento(String mensaje) {
        Platform.runLater(() -> {
            if (!cerrado) {
                actualizarContador(leerContador());
                agregarLog(mensaje);
            }
        });
    }

    private int leerContador() {
        // El mismo monitor que usa incrementarSincronizado() garantiza la visibilidad.
        synchronized (contador) {
            return contador.getTotal();
        }
    }

    private void esperarFinalizacion(List<Thread> hilosActuales) {
        boolean interrumpido = false;
        for (Thread hilo : hilosActuales) {
            boolean terminado = false;
            while (!terminado) {
                try {
                    hilo.join();
                    terminado = true;
                } catch (InterruptedException e) {
                    // Aun si interrumpen al coordinador, no habilitar un inicio prematuro.
                    interrumpido = true;
                }
            }
        }
        if (interrumpido) {
            Thread.currentThread().interrupt();
        }
        Platform.runLater(() -> {
            if (cerrado) {
                return;
            }
            enEjecucion = false;
            String estadoFinal = detencionSolicitada || huboError ? "Detenido" : "Finalizado";
            actualizarContador(leerContador());
            actualizarEstado(estadoFinal, false, false);
            agregarLog(estadoFinal + ". Todos los hilos terminaron. Total: " + leerContador() + " turnos.");
        });
    }

    private void actualizarEstado(String texto, boolean ejecutando, boolean deteniendo) {
        estado.setText(texto);
        botonIniciar.setDisable(ejecutando);
        botonDetener.setDisable(!ejecutando || deteniendo);
        indicacion.setText(switch (texto) {
            case "Ejecutando" -> "Atendiendo turnos. Podés detener la simulación cuando quieras.";
            case "Deteniendo" -> "Esperando que terminen las ventanillas…";
            case "Detenido", "Finalizado" -> "Podés iniciar otra vez. El contador volverá a cero.";
            default -> "Presioná Iniciar para comenzar.";
        });
    }

    private void actualizarContador(int total) {
        contadorLabel.setText(Integer.toString(total));
        progreso.setProgress((double) total / TOTAL_TURNOS);
    }

    private void agregarLog(String mensaje) {
        logs.appendText("[" + LocalTime.now().format(HORA) + "] " + mensaje + System.lineSeparator());
    }

    public void cerrar() {
        if (!cerrado) {
            cerrado = true;
            solicitarDetencion();
        }
    }
}
