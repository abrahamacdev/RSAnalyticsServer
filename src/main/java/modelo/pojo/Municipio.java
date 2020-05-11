package modelo.pojo;

import modelo.pojo.scrapers.Anuncio;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table( name = "municipio",
        uniqueConstraints = { @UniqueConstraint( columnNames = { "nombre"} ) }
        )
public class Municipio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre")
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER,
            cascade = { CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "provincia_id")
    private Provincia provincia;

    public Municipio(){}

    public Municipio(String nombre, Provincia provincia) {
        this.nombre = nombre;
        this.provincia = provincia;
    }

    @Override
    public String toString() {
        return "(Municipio) " + nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Municipio municipio = (Municipio) o;

        if (id == 0 || municipio.id == 0){
            return Objects.equals(nombre, municipio.nombre);
        }

        return Objects.equals(id, municipio.id);
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

    public Provincia getProvincia() {
        return provincia;
    }

    public void setProvincia(Provincia provincia) {
        this.provincia = provincia;
    }
}
