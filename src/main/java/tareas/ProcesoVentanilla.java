package tareas;

public class ProcesoVentanilla implements Runnable {

    private final String nombre;
    private final modelo.ContadorCompartido contador;

    public ProcesoVentanilla(String nombre) {
        this.nombre = nombre;
        this.contador = null;
    }

    public ProcesoVentanilla(String nombre, modelo.ContadorCompartido contador) {
        this.nombre = nombre;
        this.contador = contador;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public void run() {
        for (int i = 1; i <= 5; i++) {
            System.out.println(nombre + " procesando turno " + i);
            if (contador != null) {
                contador.incrementar(); // Incrementa el contador compartido
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println(nombre + " interrumpido.");
                Thread.currentThread().interrupt(); // Buena práctica
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            System.out.println("[" + Thread.currentThread().getName() + "] " + nombre + " procesa " + i);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}