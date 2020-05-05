package utilidades;

import modelo.pojo.scrapers.Procedencia;

import java.util.Objects;

public class Par<T,E> {

    private T primero;
    private E segundo;

    public Par(T primero, E segundo){
        this.primero = primero;
        this.segundo = segundo;
    }

    public T getPrimero() {
        return primero;
    }

    public E getSegundo() {
        return segundo;
    }

    @Override
    public String toString() {
        String msgPrimero = this.primero == null ? "" : this.primero.toString();
        String msgSegundo = this.segundo == null ? "" : this.segundo.toString();
        return "Primero: " + msgPrimero + " -- Segundo: " + msgSegundo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Par par = (Par) o;
        return Objects.equals(primero, par.primero) &&
                Objects.deepEquals(segundo, par.segundo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primero,segundo);
    }
}
