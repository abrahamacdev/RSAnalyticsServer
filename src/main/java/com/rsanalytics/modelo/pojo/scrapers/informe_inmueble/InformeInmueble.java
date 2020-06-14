package com.rsanalytics.modelo.pojo.scrapers.informe_inmueble;

import com.rsanalytics.modelo.pojo.scrapers.Informe;
import com.rsanalytics.modelo.pojo.scrapers.Inmueble;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "inmueble_informe")
public class InformeInmueble {

    @EmbeddedId
    private InformeInmuebleId informeInmuebleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idInmueble") // Nombre de la variable en la clase "InformeInmuebleId.java"
    private Inmueble inmueble;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idInforme") // Nombre de la variable en la clase "InformeInmuebleId.java"
    private Informe informe;

    public InformeInmueble(){}

    public InformeInmueble(Inmueble inmueble, Informe informe) {
        this.inmueble = inmueble;
        this.informe = informe;
        this.informeInmuebleId = new InformeInmuebleId(informe.getId(), inmueble.getId());
    }

    public InformeInmueble(InformeInmuebleId informeInmuebleId, Inmueble inmueble, Informe informe) {
        this.informeInmuebleId = informeInmuebleId;
        this.inmueble = inmueble;
        this.informe = informe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        InformeInmueble that = (InformeInmueble) o;
        return Objects.equals(informeInmuebleId, that.informeInmuebleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(informeInmuebleId);
    }

    public InformeInmuebleId getInformeInmuebleId() {
        return informeInmuebleId;
    }

    @Transient
    public void setInformeInmuebleId(InformeInmuebleId informeInmuebleId) {
        this.informeInmuebleId = informeInmuebleId;
    }

    public Inmueble getInmueble() {
        return inmueble;
    }

    public void setInmueble(Inmueble inmueble) {
        if (informeInmuebleId != null){
            informeInmuebleId.setIdInmueble(inmueble.getId());
        }
        this.inmueble = inmueble;
    }

    public Informe getInforme() {
        return informe;
    }

    public void setInforme(Informe informe) {
        if (informeInmuebleId != null){
            informeInmuebleId.setIdInforme(informe.getId());
        }
        this.informe = informe;
    }
}
