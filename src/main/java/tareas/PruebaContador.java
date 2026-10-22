package tareas;

import modelo.ContadorCompartido;

public class PruebaContador {

    private static final int INCREMENTOS = 1000;
    private static final int REPETICIONES = 10;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== PRUEBA DE CONDICIÓN DE CARRERA (Integrante 3) ===\n");

        probarSinSincronizacion();
        probarConSincronizacion();
        probarConAtomicInteger();
    }

    //1. Sin SINCRONIZACIÓN
    private static void probarSinSincronizacion() throws InterruptedException {
        System.out.println("--- 1. SIN SINCRONIZACIÓN (Esperado: 2000) ---");
        System.out.printf("%-10s %-15s %-15s%n", "Ejecución", "Resultado", "¿Correcto?");
        int fallos = 0;

        for (int i = 1; i <= REPETICIONES; i++) {
            ContadorCompartido c = new ContadorCompartido();
            Thread h1 = new Thread(() -> { for (int j = 0; j < INCREMENTOS; j++) c.incrementar(); }, "Hilo-1");
            Thread h2 = new Thread(() -> { for (int j = 0; j < INCREMENTOS; j++) c.incrementar(); }, "Hilo-2");

            h1.start(); h2.start();
            h1.join();  h2.join();

            int res = c.getTotal();
            boolean ok = (res == 2000);
            if (!ok) fallos++;
            System.out.printf("%-10d %-15d %-15s%n", i, res, ok ? "SÍ" : "NO");
        }
        System.out.println("Fallos: " + fallos + "/" + REPETICIONES + "\n");
    }

    //2. Con SYNCHRONIZED
    private static void probarConSincronizacion() throws InterruptedException {
        System.out.println("--- 2. CON synchronized (Esperado: 2000) ---");
        System.out.printf("%-10s %-15s %-15s%n", "Ejecución", "Resultado", "¿Correcto?");
        int fallos = 0;

        for (int i = 1; i <= REPETICIONES; i++) {
            ContadorCompartido c = new ContadorCompartido();

            Thread t1 = new Thread(() -> { for (int k = 0; k < INCREMENTOS; k++) c.incrementarSincronizado(); }, "Hilo-1");
            Thread t2 = new Thread(() -> { for (int k = 0; k < INCREMENTOS; k++) c.incrementarSincronizado(); }, "Hilo-2");

            t1.start(); t2.start();
            t1.join();  t2.join();

            int res = c.getTotal();
            boolean ok = (res == 2000);
            if (!ok) fallos++;
            System.out.printf("%-10d %-15d %-15s%n", i, res, ok ? "SÍ" : "NO");
        }
        System.out.println("Fallos: " + fallos + "/" + REPETICIONES + "\n");
    }

    //3. Con ATOMIC INTEGER
    private static void probarConAtomicInteger() throws InterruptedException {
        System.out.println("--- 3. CON AtomicInteger (Esperado: 2000) ---");
        System.out.printf("%-10s %-15s %-15s%n", "Ejecución", "Resultado", "¿Correcto?");
        int fallos = 0;

        for (int i = 1; i <= REPETICIONES; i++) {
            ContadorCompartido c = new ContadorCompartido();

            Thread p1 = new Thread(() -> { for (int m = 0; m < INCREMENTOS; m++) c.incrementarAtomico(); }, "Hilo-1");
            Thread p2 = new Thread(() -> { for (int m = 0; m < INCREMENTOS; m++) c.incrementarAtomico(); }, "Hilo-2");

            p1.start(); p2.start();
            p1.join();  p2.join();

            int res = c.getTotalAtomico();
            boolean ok = (res == 2000);
            if (!ok) fallos++;
            System.out.printf("%-10d %-15d %-15s%n", i, res, ok ? "SÍ" : "NO");
        }
        System.out.println("Fallos: " + fallos + "/" + REPETICIONES);
    }
}