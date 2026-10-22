package modelo;

import java.util.concurrent.atomic.AtomicInteger;

public class ContadorCompartido {

    private int total = 0;
    private AtomicInteger totalAtomico = new AtomicInteger(0);

    public void incrementar() {
        total++;
    }
    public synchronized void incrementarSincronizado() {
        total++;
    }

    public void incrementarAtomico() {
        totalAtomico.incrementAndGet();
    }

    public int getTotal() { return total; }
    public int getTotalAtomico() { return totalAtomico.get(); }

    public void reiniciar() {
        total = 0;
        totalAtomico.set(0);
    }
}
