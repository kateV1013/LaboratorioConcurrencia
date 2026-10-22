package tareas;

public class PruebaVentanillas {

    public static void ejecutarConStart() {
        System.out.println("=== EJECUCIÓN CON start() (HILOS INDEPENDIENTES) ===");
        Thread hiloA = new Thread(new ProcesoVentanilla("Ventanilla-A"), "Ventanilla-A");
        Thread hiloB = new Thread(new ProcesoVentanilla("Ventanilla-B"), "Ventanilla-B");

        System.out.println("[Main: " + Thread.currentThread().getName() + "] Llamando a start() para Ventanilla-A y Ventanilla-B...");
        hiloA.start();
        hiloB.start();
        System.out.println("[Main: " + Thread.currentThread().getName() + "] start() retornó de inmediato (no bloqueante).");

        try {
            hiloA.join();
            hiloB.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("[Main: " + Thread.currentThread().getName() + "] Ambas ventanillas han terminado.\n");
    }

    public static void main(String[] args) {
        ejecutarConStart();
    }
}
