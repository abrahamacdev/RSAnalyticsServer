package modelo.pojo.scrapers;

import modelo.pojo.rest.Tipo;
import utilidades.Utils;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "tipoContrato")
public class TipoContrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre")
    private String nombre;

    public TipoContrato(){}

    public TipoContrato(String nombre){
        this.nombre = nombre;
    }

    public static TipoContrato obtenerTipoContratoConNombre(String nombre){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();

        TipoContrato res = null;

        try {

            Query query = entityManager.createQuery("FROM TipoContrato AS tip WHERE tip.nombre = :nombre", TipoContrato.class);
            query.setParameter("nombre", nombre);
            res = (TipoContrato) query.getSingleResult();

        }catch (Exception e){

        }

        entityTransaction.commit();
        entityManager.close();

        return res;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoContrato tipoContrato = (TipoContrato) o;

        if (id == 0 || tipoContrato.id == 0){
            return Objects.equals(nombre, tipoContrato.nombre);
        }

        return Objects.equals(id, tipoContrato.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
