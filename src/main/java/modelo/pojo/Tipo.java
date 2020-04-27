package modelo.pojo;

import utilidades.Utils;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tipo")
public class Tipo {

    public enum NOMBRE {
        INVITACION(1);

        private final int id;
        NOMBRE (int id){
            this.id = id;
        }

        public int getId(){
            return id;
        };
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", unique = true)
    private String nombre;

    public Tipo() {}

    public Tipo(String nombre) {
        this.nombre = nombre;
    }

    public Tipo(NOMBRE nombreTipo){
        obtenerTipoAccion(nombreTipo);
    }

    public static Tipo obtenerTipoAccion(NOMBRE nombreTipo){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();

        Query query = entityManager.createQuery("FROM Tipo AS tp WHERE tp.id = :id");
        query.setParameter("id", nombreTipo.id);

        Tipo tipo = (Tipo) query.getResultList().get(0);

        entityTransaction.commit();
        entityManager.close();

        return tipo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tipo tipo = (Tipo) o;
        return Objects.equals(id, tipo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Tipo: " + nombre + " - Id: " + id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
