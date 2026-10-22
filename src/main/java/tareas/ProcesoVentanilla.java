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
        // Clase base para implementar la tarea concurrente de la ventanilla
    }
}
