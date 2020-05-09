package modelo.pojo.scrapers.anuncio_inmueble_tipoContrato;

import modelo.pojo.scrapers.atributo_anuncio.AtributoAnuncio;
import modelo.pojo.scrapers.atributo_anuncio.AtributoAnuncioId;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Objects;

@Entity
@Table(name = "anuncio_tipoContrato_inmueble")
public class AnuncioInmuebleTipoContrato {

    @EmbeddedId
    private AtributoAnuncioId atributoAnuncioId;

    public AnuncioInmuebleTipoContrato(){}

    public AnuncioInmuebleTipoContrato(AtributoAnuncioId atributoAnuncioId) {
        this.atributoAnuncioId = atributoAnuncioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        AnuncioInmuebleTipoContrato that = (AnuncioInmuebleTipoContrato) o;
        return Objects.equals(atributoAnuncioId, that.atributoAnuncioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(atributoAnuncioId);
    }

    public AtributoAnuncioId getAtributoAnuncioId() {
        return atributoAnuncioId;
    }

    @Transient
    public void setAtributoAnuncioId(AtributoAnuncioId atributoAnuncioId) {
        this.atributoAnuncioId = atributoAnuncioId;
    }
}
