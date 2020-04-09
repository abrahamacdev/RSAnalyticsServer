package utilidades;

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
}
