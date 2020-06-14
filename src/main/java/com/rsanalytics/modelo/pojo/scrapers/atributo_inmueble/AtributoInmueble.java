package com.rsanalytics.modelo.pojo.scrapers.atributo_inmueble;

import com.rsanalytics.modelo.pojo.scrapers.ClaveAtributoInmueble;
import com.rsanalytics.modelo.pojo.scrapers.Inmueble;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "atributoInmueble")
public class AtributoInmueble {

    @EmbeddedId
    private AtributoInmuebleId atributoInmuebleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idInmueble") // Nombre de la variable en la clase "AtributoInmuebleId.java"
    private Inmueble inmueble;

    @ManyToOne(fetch = FetchType.EAGER,
                cascade = { CascadeType.MERGE, CascadeType.REFRESH }
                )
    @MapsId("idClaveAtributoInmueble") // Nombre de la variable en la clase "AtributoInmuebleId.java"
    private ClaveAtributoInmueble claveAtributoInmueble;

    @Column(name = "valor_cadena")
    private String valorCadena;

    @Column(name = "valor_numerico")
    private double valorNumerico;


    public AtributoInmueble(){}

    public AtributoInmueble(Inmueble inmueble, ClaveAtributoInmueble claveAtributoInmueble){
        this(inmueble, claveAtributoInmueble, null);
    }

    public AtributoInmueble(Inmueble inmueble, ClaveAtributoInmueble claveAtributoInmueble, double valorNumerico) {
        this.atributoInmuebleId= new AtributoInmuebleId(inmueble.getId(), claveAtributoInmueble.getId());
        this.inmueble = inmueble;
        this.claveAtributoInmueble = claveAtributoInmueble;
        this.valorNumerico = valorNumerico;
    }

    public AtributoInmueble(Inmueble inmueble, ClaveAtributoInmueble claveAtributoInmueble, String valorCadena) {
        this.atributoInmuebleId= new AtributoInmuebleId(inmueble.getId(), claveAtributoInmueble.getId());
        this.inmueble = inmueble;
        this.claveAtributoInmueble = claveAtributoInmueble;
        this.valorCadena = valorCadena;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        AtributoInmueble that = (AtributoInmueble) o;
        return Objects.equals(inmueble, that.inmueble) &&
                Objects.equals(claveAtributoInmueble, that.claveAtributoInmueble);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inmueble, claveAtributoInmueble);
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

    public AtributoInmuebleId getAtributoInmuebleId() {
        return atributoInmuebleId;
    }

    public void setAtributoInmuebleId(AtributoInmuebleId atributoInmuebleId) {
        this.atributoInmuebleId = atributoInmuebleId;
    }

    public Inmueble getInmueble() {
        return inmueble;
    }

    public void setInmueble(Inmueble inmueble) {
        if (this.atributoInmuebleId != null){
            this.atributoInmuebleId.setIdInmueble(inmueble.getId());
        }

        this.inmueble = inmueble;
    }

    public ClaveAtributoInmueble getClaveAtributoInmueble() {
        return claveAtributoInmueble;
    }

    public void setClaveAtributoInmueble(ClaveAtributoInmueble claveAtributoInmueble) {
        if (this.atributoInmuebleId != null){
            this.atributoInmuebleId.setIdClaveAtributoInmueble(claveAtributoInmueble.getId());
        }

        this.claveAtributoInmueble = claveAtributoInmueble;
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
