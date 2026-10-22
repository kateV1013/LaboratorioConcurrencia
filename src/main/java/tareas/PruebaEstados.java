package tareas;

public class PruebaEstados {

    private static final long INTERVALO_MONITOR_MS = 20;

    public static void main(String[] args) throws InterruptedException {
        probarCicloDeVida();
        probarRunnableSostenido();
        probarBlocked();
        probarWaiting();
    }

    private static void probarCicloDeVida() throws InterruptedException {
        System.out.println("\n--- Ciclo de vida de Ventanilla-A ---");

        // el monitor ve NEW antes del start
        ProcesoVentanilla proceso = new ProcesoVentanilla("Ventanilla-A");
        Thread ventanilla = new Thread(() -> {
            proceso.run();
            // carga corta para ver RUNNABLE
            trabajarDurante(100);
            // pausa para ver TIMED_WAITING
            esperarDurante(300);
        }, "Ventanilla-A");

        System.out.println("Antes de start(): " + ventanilla.getState());
        Thread monitor = MonitorHilos.iniciar(ventanilla, INTERVALO_MONITOR_MS, System.out::println);
        Thread.sleep(100);
        ventanilla.start();
        System.out.println("Después de start(): " + ventanilla.getState());
        monitor.join();
    }

    private static void probarRunnableSostenido() throws InterruptedException {
        System.out.println("\n--- RUNNABLE sostenido ---");

        // El bucle ocupado no espera ni cede el candado; por eso permanece RUNNABLE.
        Thread ocupado = new Thread(() -> trabajarDurante(400), "Hilo-Ocupado");
        Thread monitor = MonitorHilos.iniciar(ocupado, INTERVALO_MONITOR_MS, System.out::println);
        ocupado.start();
        monitor.join();
    }

    private static void probarBlocked() throws InterruptedException {
        System.out.println("\n--- BLOCKED ---");

        // main retiene el candado mientras el hilo intenta entrar al bloque synchronized.
        Object candado = new Object();
        Thread bloqueado = new Thread(() -> {
            synchronized (candado) {
                // Espera adquirir el candado que retiene main.
            }
        }, "Hilo-Blocked");
        Thread monitor = MonitorHilos.iniciar(bloqueado, INTERVALO_MONITOR_MS, System.out::println);

        synchronized (candado) {
            bloqueado.start();
            esperarEstado(bloqueado, Thread.State.BLOCKED);
            trabajarDurante(100);
        }
        monitor.join();
    }

    private static void probarWaiting() throws InterruptedException {
        System.out.println("\n--- WAITING ---");

        // wait() libera el candado y deja el hilo en WAITING hasta recibir notify().
        Object candado = new Object();
        Thread enEspera = new Thread(() -> {
            synchronized (candado) {
                try {
                    candado.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Hilo-Waiting");
        Thread monitor = MonitorHilos.iniciar(enEspera, INTERVALO_MONITOR_MS, System.out::println);
        enEspera.start();

        esperarEstado(enEspera, Thread.State.WAITING);
        trabajarDurante(100);
        synchronized (candado) {
            candado.notify();
        }
        monitor.join();
    }

    private static void esperarEstado(Thread hilo, Thread.State estadoEsperado) {
        long limite = System.nanoTime() + 2_000_000_000L;
        while (hilo.getState() != estadoEsperado && System.nanoTime() < limite) {
            Thread.onSpinWait();
        }
        if (hilo.getState() != estadoEsperado) {
            throw new IllegalStateException(
                    "El hilo " + hilo.getName() + " no alcanzó el estado " + estadoEsperado
            );
        }
    }

    private static void trabajarDurante(long duracionMs) {
        long limite = System.nanoTime() + duracionMs * 1_000_000L;
        while (System.nanoTime() < limite) {
            Thread.onSpinWait();
        }
    }

    private static void esperarDurante(long duracionMs) {
        try {
            Thread.sleep(duracionMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
    }
}
