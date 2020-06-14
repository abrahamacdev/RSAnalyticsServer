package com.rsanalytics.modelo.pojo.scrapers.anuncio_inmueble_tipoContrato;

import com.rsanalytics.modelo.pojo.scrapers.Anuncio;
import com.rsanalytics.modelo.pojo.scrapers.Inmueble;
import com.rsanalytics.modelo.pojo.scrapers.TipoContrato;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "anuncio_tipoContrato_inmueble")
public class AnuncioInmuebleTipoContrato {

    @EmbeddedId
    private AnuncioInmuebleTipoContratoId anuncioInmuebleTipoContratoId;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idInmueble") // Nombre de la variable en la clase "AnuncioInmuebleTipoContratoId.java"
    private Inmueble inmueble;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idAnuncio") // Nombre de la variable en la clase "AnuncioInmuebleTipoContratoId.java"
    private Anuncio anuncio;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idTipoContrato") // Nombre de la variable en la clase "AnuncioInmuebleTipoContratoId.java"
    private TipoContrato tipoContrato;


    public AnuncioInmuebleTipoContrato(){}

    public AnuncioInmuebleTipoContrato(Anuncio anuncio, Inmueble inmueble, TipoContrato tipoContrato) {
        this.anuncioInmuebleTipoContratoId = new AnuncioInmuebleTipoContratoId(anuncio.getId(), inmueble.getId(), tipoContrato.getId());
        this.anuncio = anuncio;
        this.inmueble = inmueble;
        this.tipoContrato = tipoContrato;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        AnuncioInmuebleTipoContrato that = (AnuncioInmuebleTipoContrato) o;
        return Objects.equals(anuncioInmuebleTipoContratoId, that.anuncioInmuebleTipoContratoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anuncioInmuebleTipoContratoId);
    }

    public AnuncioInmuebleTipoContratoId getAnuncioInmuebleTipoContratoId() {
        return anuncioInmuebleTipoContratoId;
    }

    @Transient
    public void setAnuncioInmuebleTipoContratoId(AnuncioInmuebleTipoContratoId anuncioInmuebleTipoContratoId) {
        this.anuncioInmuebleTipoContratoId = anuncioInmuebleTipoContratoId;
    }

    public Inmueble getInmueble() {
        return inmueble;
    }

    public void setInmueble(Inmueble inmueble) {
        if (anuncioInmuebleTipoContratoId != null){
            anuncioInmuebleTipoContratoId.setIdInmueble(inmueble.getId());
        }

        this.inmueble = inmueble;
    }

    public Anuncio getAnuncio() {
        return anuncio;
    }

    public void setAnuncio(Anuncio anuncio) {
        if (anuncioInmuebleTipoContratoId != null){
            anuncioInmuebleTipoContratoId.setIdAnuncio(anuncio.getId());
        }
        this.anuncio = anuncio;
    }

    public TipoContrato getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(TipoContrato tipoContrato) {
        if (anuncioInmuebleTipoContratoId != null){
            anuncioInmuebleTipoContratoId.setIdTipoContrato(tipoContrato.getId());
        }
        this.tipoContrato = tipoContrato;
    }
}
