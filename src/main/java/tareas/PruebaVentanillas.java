package tareas;

public class PruebaVentanillas {

    public static void ejecutarConRun() {
        System.out.println("============================================================");
        System.out.println("=== 1. EJECUCION DIRECTA CON run() (SECUENCIAL / BLOQUEANTE) ===");
        System.out.println("============================================================");
        Runnable tareaA = new ProcesoVentanilla("Ventanilla-A");
        Runnable tareaB = new ProcesoVentanilla("Ventanilla-B");

        System.out.println("[Main: " + Thread.currentThread().getName() + "] Invocando tareaA.run() directamente...");
        tareaA.run();
        System.out.println("[Main: " + Thread.currentThread().getName() + "] tareaA.run() finalizo. Invocando tareaB.run()...");
        tareaB.run();
        System.out.println("[Main: " + Thread.currentThread().getName() + "] Ambas tareas ejecutadas secuencialmente en el hilo principal.\n");
    }

    public static void ejecutarConStart() {
        System.out.println("============================================================");
        System.out.println("=== 2. EJECUCION CON start() (CONCURRENTE / NO BLOQUEANTE)   ===");
        System.out.println("============================================================");
        Thread hiloA = new Thread(new ProcesoVentanilla("Ventanilla-A"), "Ventanilla-A");
        Thread hiloB = new Thread(new ProcesoVentanilla("Ventanilla-B"), "Ventanilla-B");

        System.out.println("[Main: " + Thread.currentThread().getName() + "] Invocando start() para Ventanilla-A y Ventanilla-B...");
        hiloA.start();
        hiloB.start();
        System.out.println("[Main: " + Thread.currentThread().getName() + "] start() retorno de inmediato. El hilo main continua libre.");

        try {
            hiloA.join();
            hiloB.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("[Main: " + Thread.currentThread().getName() + "] Ambos hilos terminaron mediante join().\n");
    }

    public static void main(String[] args) {
        ejecutarConRun();
        ejecutarConStart();
    }
}
