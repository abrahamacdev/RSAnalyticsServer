package utilidades;

public class Tripleta<T,E,K> {

    private T primero;
    private E segundo;
    private K tercero;

    public Tripleta(T primero, E segundo, K k){
        this.primero = primero;
        this.segundo = segundo;
        this.tercero = k;
    }

    public boolean algunoEsNulo(){
        return primero == null || segundo == null || tercero == null;
    }

    public T getPrimero() {
        return primero;
    }

    public E getSegundo() {
        return segundo;
    }

    public K getTercero() {
        return tercero;
    }
}
