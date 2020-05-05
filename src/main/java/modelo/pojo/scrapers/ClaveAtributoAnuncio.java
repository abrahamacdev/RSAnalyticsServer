package modelo.pojo.scrapers;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "claveAtributoAnuncio")
public class ClaveAtributoAnuncio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre")
    private String nombre;

    public ClaveAtributoAnuncio(){}

    public ClaveAtributoAnuncio(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaveAtributoAnuncio claveAtributoAnuncio = (ClaveAtributoAnuncio) o;
        return Objects.equals(id, claveAtributoAnuncio.id);
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
