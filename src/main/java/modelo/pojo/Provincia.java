package modelo.pojo;

import modelo.pojo.scrapers.Anuncio;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table( name = "provincia")
public class Provincia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre")
    private String nombre;

    public Provincia(){}

    public Provincia(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Provincia \'" + nombre + "\'";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Provincia provincia = (Provincia) o;

        if (id == 0 || provincia.id == 0){
            return Objects.equals(nombre, provincia.nombre);
        }

        return Objects.equals(id, provincia.id);
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
