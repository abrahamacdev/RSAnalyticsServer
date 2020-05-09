package modelo.pojo.scrapers;

import modelo.pojo.Municipio;
import modelo.pojo.scrapers.atributo_anuncio.AtributoAnuncio;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "anuncio")
public class Anuncio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @OneToMany(fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            mappedBy = "anuncio" // Nombre de la variable en la clase "AtributoInmueble.java"
    )
    private Set<AtributoAnuncio> atributos = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "procedencia_id")
    private Procedencia procedencia;

    @Column(name = "fecha_obtencion")
    private Date fechaObtencion;

    @ManyToOne(fetch = FetchType.EAGER,
                cascade = { CascadeType.REFRESH, CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;


    public Anuncio(){}

    public Anuncio(HashSet<AtributoAnuncio> atributos, Procedencia procedencia) {
        this.atributos = atributos;
        this.procedencia = procedencia;
    }

    public Anuncio(HashSet<AtributoAnuncio> atributos, Procedencia procedencia, Municipio municipio) {
        this.atributos = atributos;
        this.procedencia = procedencia;
        this.municipio = municipio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Anuncio anuncio = (Anuncio) o;
        return Objects.equals(id, anuncio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String tempProc = procedencia != null ? procedencia.getNombre() : "Desconocida";
        String tempAtributos = "";

        for (AtributoAnuncio atributo : atributos) {
            tempAtributos += atributo + ", ";
        }

        tempAtributos = tempAtributos.substring(0, tempAtributos.length() - 1);

        return "Anuncio procedente de \'" + tempProc + "\'. Atributos (" + tempAtributos + ")";
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Procedencia getProcedencia() {
        return procedencia;
    }

    public void setProcedencia(Procedencia procedencia) {
        this.procedencia = procedencia;
    }

    public Set<AtributoAnuncio> getAtributos() {
        return atributos;
    }

    public void setAtributos(Set<AtributoAnuncio> atributos) {
        this.atributos = atributos;
    }

    public Date getFechaObtencion() {
        return fechaObtencion;
    }

    public void setFechaObtencion(Date fechaObtencion) {
        this.fechaObtencion = fechaObtencion;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }
}
