package modelo.pojo.scrapers.atributo_anuncio;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AtributoAnuncioId implements Serializable {

    @Column(name = "anuncio_id")
    private int idAnuncio;

    @Column(name = "claveAtributoAnuncio_id")
    private int idClaveAtributoAnuncio;

    public AtributoAnuncioId(){}

    public AtributoAnuncioId(int idAnuncio, int idClaveAtributoAnuncio) {
        this.idAnuncio = idAnuncio;
        this.idClaveAtributoAnuncio = idClaveAtributoAnuncio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        AtributoAnuncioId that = (AtributoAnuncioId) o;
        return Objects.equals(idAnuncio, that.idAnuncio) &&
                Objects.equals(idClaveAtributoAnuncio, that.idClaveAtributoAnuncio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAnuncio, idClaveAtributoAnuncio);
    }

    public int getIdAnuncio() {
        return idAnuncio;
    }

    public void setIdAnuncio(int idAnuncio) {
        this.idAnuncio = idAnuncio;
    }

    public int getIdClaveAtributoAnuncio() {
        return idClaveAtributoAnuncio;
    }

    public void setIdClaveAtributoAnuncio(int idClaveAtributoAnuncio) {
        this.idClaveAtributoAnuncio = idClaveAtributoAnuncio;
    }
}
