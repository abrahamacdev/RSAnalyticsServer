package modelo.pojo.scrapers.anuncio_inmueble_tipoContrato;

import modelo.pojo.scrapers.atributo_anuncio.AtributoAnuncioId;
import org.apache.commons.lang3.ObjectUtils;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AnuncioInmuebleTipoContratoId implements Serializable {


    @Column(name = "anuncio_id")
    private int idAnuncio;

    @Column(name = "inmueble_id")
    private int idInmueble;

    @Column(name = "tipoContrato_id")
    private int idTipoContrato;

    public AnuncioInmuebleTipoContratoId(){}

    public AnuncioInmuebleTipoContratoId(int idAnuncio, int idInmueble, int idTipoContrato) {
        this.idAnuncio = idAnuncio;
        this.idInmueble = idInmueble;
        this.idTipoContrato = idTipoContrato;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        AnuncioInmuebleTipoContratoId that = (AnuncioInmuebleTipoContratoId) o;
        return Objects.equals(idAnuncio, that.idAnuncio) &&
                Objects.equals(idInmueble, that.idInmueble) &&
                Objects.equals(idTipoContrato, that.idTipoContrato);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAnuncio, idInmueble, idTipoContrato);
    }

    public int getIdAnuncio() {
        return idAnuncio;
    }

    public void setIdAnuncio(int idAnuncio) {
        this.idAnuncio = idAnuncio;
    }

    public int getIdInmueble() {
        return idInmueble;
    }

    public void setIdInmueble(int idInmueble) {
        this.idInmueble = idInmueble;
    }

    public int getIdTipoContrato() {
        return idTipoContrato;
    }

    public void setIdTipoContrato(int idTipoContrato) {
        this.idTipoContrato = idTipoContrato;
    }
}
