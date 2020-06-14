package com.rsanalytics.modelo.pojo.scrapers.atributo_inmueble;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AtributoInmuebleId implements Serializable {

    @Column(name = "inmueble_id")
    private int idInmueble;

    @Column(name = "claveAtributoInmueble_id")
    private int idClaveAtributoInmueble;

    public AtributoInmuebleId(){}

    public AtributoInmuebleId(int idInmueble, int idClaveAtributoInmueble) {
        this.idInmueble = idInmueble;
        this.idClaveAtributoInmueble = idClaveAtributoInmueble;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        AtributoInmuebleId that = (AtributoInmuebleId) o;
        return Objects.equals(idInmueble, that.idInmueble) &&
                Objects.equals(idClaveAtributoInmueble, that.idClaveAtributoInmueble);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idInmueble, idClaveAtributoInmueble);
    }

    @Override
    public String toString() {
        return "AtributoInmueble formado por inmueble(" + idInmueble + ") y claveAtributoInmueble(" + idClaveAtributoInmueble + ")";
    }

    public int getIdInmueble() {
        return idInmueble;
    }

    public void setIdInmueble(int idInmueble) {
        this.idInmueble = idInmueble;
    }

    public int getIdClaveAtributoInmueble() {
        return idClaveAtributoInmueble;
    }

    public void setIdClaveAtributoInmueble(int idClaveAtributoInmueble) {
        this.idClaveAtributoInmueble = idClaveAtributoInmueble;
    }
}
