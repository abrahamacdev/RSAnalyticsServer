package com.rsanalytics.modelo.pojo.scrapers;

import com.rsanalytics.modelo.pojo.Municipio;
import com.rsanalytics.modelo.pojo.scrapers.atributo_inmueble.AtributoInmueble;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "inmueble")
public class Inmueble {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            mappedBy = "inmueble" // Nombre de la variable en la clase "AtributoInmueble.java"
    )
    private Set<AtributoInmueble> atributos = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipoInmueble_id")
    private TipoInmueble tipoInmueble;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;

    @Column(name = "fecha_creacion")
    private Date fechaCreacion = new Date();

    public Inmueble() {}

    public Inmueble(TipoInmueble tipoInmueble, Municipio municipio) {
        this.tipoInmueble = tipoInmueble;
        this.municipio = municipio;
    }

    public Inmueble(HashSet<AtributoInmueble> atributos, TipoInmueble tipoInmueble, Municipio municipio) {
        this.atributos = atributos;
        this.tipoInmueble = tipoInmueble;
        this.municipio = municipio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inmueble inmueble = (Inmueble) o;

        return Objects.equals(id, inmueble.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "(Inmueble " + id + ") tiene " + atributos.size() + " atributos";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<AtributoInmueble> getAtributos() {
        return atributos;
    }

    public void setAtributos(Set<AtributoInmueble> atributos) {
        this.atributos = atributos;
    }

    public TipoInmueble getTipoInmueble() {
        return tipoInmueble;
    }

    public void setTipoInmueble(TipoInmueble tipoInmueble) {
        this.tipoInmueble = tipoInmueble;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
