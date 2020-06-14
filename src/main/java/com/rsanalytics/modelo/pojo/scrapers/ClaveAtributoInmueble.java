package com.rsanalytics.modelo.pojo.scrapers;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "claveAtributoInmueble")
public class ClaveAtributoInmueble {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "es_principal")
    private boolean esPrincipal = false;

    public ClaveAtributoInmueble(){}

    public ClaveAtributoInmueble(String nombre) {
        this.nombre = nombre;
    }

    public ClaveAtributoInmueble(String nombre, boolean esPrincipal) {
        this.nombre = nombre;
        this.esPrincipal = esPrincipal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaveAtributoInmueble claveAtributoInmueble = (ClaveAtributoInmueble) o;

        if (id == 0 || claveAtributoInmueble.id == 0){
            return Objects.equals(nombre, claveAtributoInmueble.nombre);
        }

        return Objects.equals(id, claveAtributoInmueble.id);
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
