package modelo.pojo.scrapers;

import utilidades.Utils;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "procedencia")
public class Procedencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "url")
    private String url;

    public Procedencia() {}

    public Procedencia(String nombre, String url) {
        this.nombre = nombre;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Procedencia procedencia = (Procedencia) o;
        return Objects.equals(id, procedencia.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Procedencia obtenerProcedenciaConNombre(String nombre){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();

        Procedencia res = null;

        try {

            Query query = entityManager.createQuery("FROM Procedencia AS pro WHERE pro.nombre = :nombre" );
            query.setParameter("nombre", nombre);
            res = (Procedencia) query.getSingleResult();

        }catch (Exception e){

        }

        entityTransaction.commit();
        entityManager.close();

        return res;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
