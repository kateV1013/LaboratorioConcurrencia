package modelo;

public class ContadorCompartido {

    private int total = 0;

    public void incrementar() {
        total++;
    }

    public int getTotal() {
        return total;
    }
}
