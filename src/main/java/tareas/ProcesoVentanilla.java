package tareas;

import java.util.Objects;
import java.util.function.Consumer;
import modelo.ContadorCompartido;

public class ProcesoVentanilla implements Runnable {

    public static final int TURNOS_POR_VENTANILLA = 5;

    private final String nombre;
    private final ContadorCompartido contador;
    private final Consumer<String> salida;
    private volatile boolean activo = true;

    public ProcesoVentanilla(String nombre) {
        this(nombre, null);
    }

    public ProcesoVentanilla(String nombre, ContadorCompartido contador) {
        this(nombre, contador, System.out::println);
    }

    public ProcesoVentanilla(String nombre, ContadorCompartido contador, Consumer<String> salida) {
        this.nombre = Objects.requireNonNull(nombre);
        this.contador = contador;
        this.salida = Objects.requireNonNull(salida);
    }

    public String getNombre() {
        return nombre;
    }

    public void detener() {
        activo = false;
    }

    @Override
    public void run() {
        for (int i = 1; i <= TURNOS_POR_VENTANILLA; i++) {
            if (!activo || Thread.currentThread().isInterrupted()) {
                salida.accept(nombre + " detenido.");
                return;
            }

            // La espera simula trabajo y ocurre únicamente en el hilo de la ventanilla.
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                salida.accept(nombre + " detenido por interrupción.");
                return;
            }

            if (!activo || Thread.currentThread().isInterrupted()) {
                salida.accept(nombre + " detenido.");
                return;
            }

            if (contador != null) {
                contador.incrementarSincronizado();
            }
            salida.accept("[" + Thread.currentThread().getName() + "] "
                    + nombre + " procesó el turno " + i + "/" + TURNOS_POR_VENTANILLA);
        }
        salida.accept(nombre + " finalizado.");
    }
}
