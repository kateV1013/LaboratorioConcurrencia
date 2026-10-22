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

    public static void ejecutarConStart(int numeroEjecucion) {
        System.out.println("------------------------------------------------------------");
        System.out.println(">>> EJECUCION N° " + numeroEjecucion + " CON start() (HILOS CONCURRENTES)");
        System.out.println("------------------------------------------------------------");
        Thread hiloA = new Thread(new ProcesoVentanilla("Ventanilla-A"), "Ventanilla-A");
        Thread hiloB = new Thread(new ProcesoVentanilla("Ventanilla-B"), "Ventanilla-B");

        System.out.println("[Main: " + Thread.currentThread().getName() + "] Invocando start() para Ventanilla-A y Ventanilla-B...");
        hiloA.start();
        hiloB.start();
        System.out.println("[Main: " + Thread.currentThread().getName() + "] start() retorno de inmediato. Hilos en ejecucion.");

        try {
            hiloA.join();
            hiloB.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("[Main: " + Thread.currentThread().getName() + "] Ejecucion N° " + numeroEjecucion + " completada.\n");
    }

    public static void ejecutarTresPruebasStart() {
        System.out.println("============================================================");
        System.out.println("=== 2. TRES EJECUCIONES CON start() PARA COMPARAR ORDEN  ===");
        System.out.println("============================================================");
        for (int i = 1; i <= 3; i++) {
            ejecutarConStart(i);
        }
    }

    public static void main(String[] args) {
        ejecutarConRun();
        ejecutarTresPruebasStart();
    }
}
