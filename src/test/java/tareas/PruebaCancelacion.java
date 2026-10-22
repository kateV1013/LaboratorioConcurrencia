package tareas;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import modelo.ContadorCompartido;

public class PruebaCancelacion {

    public static void main(String[] args) throws InterruptedException {
        probarDetencionAntesDeIniciar();
        probarDetencionDuranteEspera();
        probarInterrupcionInicial();
        probarInterrupcionDuranteEspera();
        System.out.println("OK: detención por bandera e interrupción, antes de iniciar y durante la espera.");
    }

    private static void probarDetencionAntesDeIniciar() throws InterruptedException {
        ContadorCompartido contador = new ContadorCompartido();
        ProcesoVentanilla proceso = new ProcesoVentanilla("Prueba", contador, mensaje -> {});
        proceso.detener();
        Thread hilo = new Thread(proceso);
        hilo.start();
        esperarTerminacion(hilo);
        verificar(contador.getTotal() == 0, "Un proceso detenido antes de iniciar atendió turnos");
    }

    private static void probarDetencionDuranteEspera() throws InterruptedException {
        ContadorCompartido contador = new ContadorCompartido();
        ProcesoVentanilla proceso = new ProcesoVentanilla("Prueba", contador, mensaje -> {});
        Thread hilo = new Thread(proceso);
        hilo.start();
        esperarPausa(hilo);
        proceso.detener(); // Comprueba la bandera sin utilizar interrupt().
        esperarTerminacion(hilo);
        verificar(contador.getTotal() == 0, "Se atendió un turno después de detener durante la espera");
    }

    private static void probarInterrupcionInicial() throws InterruptedException {
        ContadorCompartido contador = new ContadorCompartido();
        ProcesoVentanilla proceso = new ProcesoVentanilla("Prueba", contador, mensaje -> {});
        AtomicBoolean interrupcionConservada = new AtomicBoolean();
        Thread hilo = new Thread(() -> {
            Thread.currentThread().interrupt();
            proceso.run();
            interrupcionConservada.set(Thread.currentThread().isInterrupted());
        });
        hilo.start();
        esperarTerminacion(hilo);
        verificar(contador.getTotal() == 0, "Un hilo interrumpido al iniciar atendió turnos");
        verificar(interrupcionConservada.get(), "Se perdió la interrupción inicial");
    }

    private static void probarInterrupcionDuranteEspera() throws InterruptedException {
        ContadorCompartido contador = new ContadorCompartido();
        ProcesoVentanilla proceso = new ProcesoVentanilla("Prueba", contador, mensaje -> {});
        AtomicBoolean interrupcionConservada = new AtomicBoolean();
        Thread hilo = new Thread(() -> {
            proceso.run();
            interrupcionConservada.set(Thread.currentThread().isInterrupted());
        });
        hilo.start();
        esperarPausa(hilo);
        hilo.interrupt();
        esperarTerminacion(hilo);
        verificar(contador.getTotal() == 0, "Se atendió un turno después de interrumpir la espera");
        verificar(interrupcionConservada.get(), "No se restauró la interrupción al salir de sleep()");
    }

    private static void esperarPausa(Thread hilo) {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (hilo.getState() != Thread.State.TIMED_WAITING && System.nanoTime() < limite) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
        verificar(hilo.getState() == Thread.State.TIMED_WAITING, "La ventanilla no entró en espera");
    }

    private static void esperarTerminacion(Thread hilo) throws InterruptedException {
        hilo.join(2000);
        verificar(!hilo.isAlive(), "La ventanilla no terminó después de cancelar");
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }
}
