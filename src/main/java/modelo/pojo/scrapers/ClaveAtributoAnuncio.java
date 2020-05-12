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

    @Column(name = "es_principal")
    private boolean esPrincipal = false;

    public ClaveAtributoAnuncio(){}

    public ClaveAtributoAnuncio(String nombre) {
        this.nombre = nombre;
    }

    public ClaveAtributoAnuncio(String nombre, boolean esPrincipal) {
        this.nombre = nombre;
        this.esPrincipal = esPrincipal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClaveAtributoAnuncio claveAtributoAnuncio = (ClaveAtributoAnuncio) o;

        if (claveAtributoAnuncio.id == 0 || id == 0){
            return Objects.equals(claveAtributoAnuncio.nombre, nombre);
        }

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

    public boolean isEsPrincipal() {
        return esPrincipal;
    }

    public void setEsPrincipal(boolean esPrincipal) {
        this.esPrincipal = esPrincipal;
    }
}
