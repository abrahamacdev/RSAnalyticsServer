package modelo.pojo.rest.usuario_grupo;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UsuarioGrupoId implements Serializable {

    @Column(name = "usuario_id")
    private int idUsuario;

    @Column(name = "grupo_id")
    private int idGrupo;

    public UsuarioGrupoId(){};

    public UsuarioGrupoId(int idUsuario, int idGrupo){
        this.idUsuario = idUsuario;
        this.idGrupo = idGrupo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        UsuarioGrupoId that = (UsuarioGrupoId) o;
        return Objects.equals(idUsuario, that.idUsuario) &&
                Objects.equals(idGrupo, that.idGrupo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario, idGrupo);
    }
}
