package modelo.pojo.scrapers;

import utilidades.Utils;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "tipoInmueble")
public class TipoInmueble {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre")
    private String nombre;


    public TipoInmueble() {}

    public TipoInmueble(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoInmueble tipoInmueble = (TipoInmueble) o;

        if (id == 0 || tipoInmueble.id == 0){
            return Objects.equals(nombre, tipoInmueble.nombre);
        }

        return Objects.equals(id, tipoInmueble.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static TipoInmueble obtenerTipoInmuebleConNombre(String nombre){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();

        TipoInmueble res = null;

        try {

            Query query = entityManager.createQuery("FROM TipoInmueble AS tip WHERE tip.nombre = :nombre" );
            query.setParameter("nombre", nombre);
            res = (TipoInmueble) query.getSingleResult();

        }catch (Exception e){

        }

        entityTransaction.commit();
        entityManager.close();

        return res;

    }

    public boolean es(utilidades.inmuebles.TipoInmueble tipoInmueble){
        return id == tipoInmueble.id;
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
