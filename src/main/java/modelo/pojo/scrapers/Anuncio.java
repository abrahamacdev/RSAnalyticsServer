package modelo.pojo.scrapers;

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
            mappedBy = "anuncio" // Nombre de la variable en la clase "AtributoAnuncio.java"
    )
    private List<AtributoAnuncio> atributos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "procedencia_id")
    private Procedencia procedencia;

    public Anuncio(){}

    public Anuncio(List<AtributoAnuncio> atributos, Procedencia procedencia) {
        this.atributos = atributos;
        this.procedencia = procedencia;
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

    public List<AtributoAnuncio> getAtributos() {
        return atributos;
    }

    public void setAtributos(List<AtributoAnuncio> atributos) {
        this.atributos = atributos;
    }
}