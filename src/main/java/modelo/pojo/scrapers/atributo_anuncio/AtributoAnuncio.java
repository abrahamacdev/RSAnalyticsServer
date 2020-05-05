package modelo.pojo.scrapers.atributo_anuncio;

import modelo.pojo.scrapers.Anuncio;
import modelo.pojo.scrapers.ClaveAtributoAnuncio;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "atributoAnuncio")
public class AtributoAnuncio {

    @EmbeddedId
    private AtributoAnuncioId atributoAnuncioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idAnuncio") // Nombre de la variable en la clase "AtributoAnuncioId.java"
    private Anuncio anuncio;

    @ManyToOne(fetch = FetchType.EAGER,
                cascade = { CascadeType.REFRESH }
                )
    @MapsId("idClaveAtributoAnuncio") // Nombre de la variable en la clase "AtributoAnuncioId.java"
    private ClaveAtributoAnuncio claveAtributoAnuncio;

    @Column(name = "valor_cadena")
    private String valorCadena;

    @Column(name = "valor_numerico")
    private double valorNumerico;


    public AtributoAnuncio(){}

    public AtributoAnuncio(Anuncio anuncio, ClaveAtributoAnuncio claveAtributoAnuncio){
        this(anuncio,claveAtributoAnuncio, null);
    }

    public AtributoAnuncio(Anuncio anuncio, ClaveAtributoAnuncio claveAtributoAnuncio, String valorCadena) {
        this.atributoAnuncioId = new AtributoAnuncioId(anuncio.getId(), claveAtributoAnuncio.getId());
        this.anuncio = anuncio;
        this.claveAtributoAnuncio = claveAtributoAnuncio;
        this.valorCadena = valorCadena;
    }

    public AtributoAnuncio(Anuncio anuncio, ClaveAtributoAnuncio claveAtributoAnuncio, double valorNumerico) {
        this.atributoAnuncioId = new AtributoAnuncioId(anuncio.getId(), claveAtributoAnuncio.getId());
        this.anuncio = anuncio;
        this.claveAtributoAnuncio = claveAtributoAnuncio;
        this.valorNumerico = valorNumerico;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        AtributoAnuncio that = (AtributoAnuncio) o;
        return Objects.equals(anuncio, that.anuncio) &&
                Objects.equals(claveAtributoAnuncio, that.claveAtributoAnuncio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anuncio, claveAtributoAnuncio);
    }

    @Override
    public String toString() {
        String clave = claveAtributoAnuncio != null ? claveAtributoAnuncio.getNombre() : "";
        return clave + "(" + claveAtributoAnuncio.getId() + ") -> " + getValorActivo();
    }

    @Transient
    public Object getValorActivo(){

        if (valorCadena != null){
            return valorCadena;
        }

        if (!Objects.isNull(valorNumerico)){
            return valorNumerico;
        }

        return "";
    }

    public AtributoAnuncioId getAtributoAnuncioId() {
        return atributoAnuncioId;
    }

    @Transient
    public void setAtributoAnuncioId(AtributoAnuncioId atributoAnuncioId) {
        this.atributoAnuncioId = atributoAnuncioId;
    }

    public Anuncio getAnuncio() {
        return anuncio;
    }

    public void setAnuncio(Anuncio anuncio) {
        this.anuncio = anuncio;
    }

    public ClaveAtributoAnuncio getClaveAtributoAnuncio() {
        return claveAtributoAnuncio;
    }

    public void setClaveAtributoAnuncio(ClaveAtributoAnuncio claveAtributoAnuncio) {
        this.claveAtributoAnuncio = claveAtributoAnuncio;
    }

    public String getValorCadena() {
        return valorCadena;
    }

    public void setValorCadena(String valorCadena) {
        this.valorCadena = valorCadena;
    }

    public double getValorNumerico() {
        return valorNumerico;
    }

    public void setValorNumerico(double valorNumerico) {
        this.valorNumerico = valorNumerico;
    }
}
