package modelo.pojo;

import modelo.pojo.scrapers.Anuncio;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table( name = "municipio",
        uniqueConstraints = { @UniqueConstraint( columnNames = { "nombre", "codigo_postal" } ) }
        )
public class Municipio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "codigo_postal")
    private String codigoPostal;

    @ManyToOne(fetch = FetchType.EAGER,
            cascade = { CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "provincia_id")
    private Provincia provincia;

    public Municipio(){}

    public Municipio(String nombre, String codigoPostal, Provincia provincia) {
        this.nombre = nombre;
        this.codigoPostal = codigoPostal;
        this.provincia = provincia;
    }

    @Override
    public String toString() {
        return "(Municipio) " + nombre + " con CP: " + codigoPostal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Municipio municipio = (Municipio) o;

        if (id == 0 || municipio.id == 0){
            return Objects.equals(nombre, municipio.nombre) &&
                    Objects.equals(codigoPostal, municipio.codigoPostal);
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

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public Provincia getProvincia() {
        return provincia;
    }

    public void setProvincia(Provincia provincia) {
        this.provincia = provincia;
    }
}
