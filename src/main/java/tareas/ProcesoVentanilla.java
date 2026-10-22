package tareas;

public class ProcesoVentanilla implements Runnable {

    private final String nombre;

    public ProcesoVentanilla(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public void run() {
        for (int i = 1; i <= 5; i++) {
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
