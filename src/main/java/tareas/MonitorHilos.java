package tareas;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MonitorHilos implements Runnable {

    private final Thread objetivo;
    private final long intervaloMs;
    private final Consumer<String> salida;
    private final long inicioNanos;
    private Thread.State estadoAnterior;

    public MonitorHilos(Thread objetivo, long intervaloMs, Consumer<String> salida) {
        this.objetivo = Objects.requireNonNull(objetivo, "El hilo objetivo no puede ser null");
        if (intervaloMs <= 0) {
            throw new IllegalArgumentException("El intervalo debe ser mayor que cero");
        }
        this.intervaloMs = intervaloMs;
        this.salida = Objects.requireNonNull(salida, "La salida no puede ser null");
        this.estadoAnterior = objetivo.getState();
        this.inicioNanos = System.nanoTime();
    }

    public static Thread iniciar(Thread objetivo, long intervaloMs, Consumer<String> salida) {
        Thread monitor = new Thread(
                new MonitorHilos(objetivo, intervaloMs, salida),
                "Monitor-" + Objects.requireNonNull(objetivo, "El hilo objetivo no puede ser null").getName()
        );
        monitor.setDaemon(true);
        monitor.start();
        return monitor;
    }

    @Override
    public void run() {
        while (true) {
            Thread.State estadoActual = objetivo.getState();
            if (estadoActual != estadoAnterior) {
                long transcurridoMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicioNanos);
                salida.accept("[+" + transcurridoMs + " ms] " + objetivo.getName() + " -> " + estadoActual);
                estadoAnterior = estadoActual;
            }

            if (estadoActual == Thread.State.TERMINATED) {
                return;
            }

            try {
                Thread.sleep(intervaloMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
