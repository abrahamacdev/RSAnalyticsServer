package modelo.pojo.scrapers.informe_inmueble;

import modelo.pojo.scrapers.atributo_inmueble.AtributoInmuebleId;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class InformeInmuebleId implements Serializable {

    @Column(name = "inmueble_id")
    private int idInmueble;

    @Column(name = "informe_id")
    private int idInforme;

    public InformeInmuebleId(){}

    public InformeInmuebleId(int idInforme, int idInmueble) {
        this.idInmueble = idInmueble;
        this.idInforme = idInforme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        InformeInmuebleId that = (InformeInmuebleId) o;
        return Objects.equals(idInmueble, that.idInmueble) &&
                Objects.equals(idInforme, that.idInforme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idInmueble, idInforme);
    }

    public int getIdInmueble() {
        return idInmueble;
    }

    public void setIdInmueble(int idInmueble) {
        this.idInmueble = idInmueble;
    }

    public int getIdInforme() {
        return idInforme;
    }

    public void setIdInforme(int idInforme) {
        this.idInforme = idInforme;
    }
}
