package modelo.pojo.usuario_grupo;

import modelo.pojo.Grupo;
import modelo.pojo.Usuario;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "usuario_grupo")
public class UsuarioGrupo {

    @EmbeddedId
    private UsuarioGrupoId usuarioGrupoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUsuario") // Nombre de la variable en la clase "UsuarioGrupoId.java"
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idGrupo") // Nombre de la variable en la clase "UsuarioGrupoId.java"
    private Grupo grupo;

    @Column(name = "fecha_ingreso")
    private Date fechaIngreso = new Date();

    private UsuarioGrupo(){}

    public UsuarioGrupo(Usuario usuario, Grupo grupo){
        this.usuario = usuario;
        this.grupo = grupo;
        this.usuarioGrupoId = new UsuarioGrupoId(usuario.getId(),grupo.getId());
    }

    public UsuarioGrupoId getUsuarioGrupoId() {
        return usuarioGrupoId;
    }

    @Transient
    public void setUsuarioGrupoId(UsuarioGrupoId usuarioGrupoId) {
        this.usuarioGrupoId = usuarioGrupoId;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        this.usuarioGrupoId.setIdUsuario(usuario.getId());
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
        this.usuarioGrupoId.setIdGrupo(grupo.getId());
    }

    public Date getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass())
            return false;

        UsuarioGrupo that = (UsuarioGrupo) o;
        return Objects.equals(usuario, that.usuario) &&
                Objects.equals(grupo, that.grupo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario, grupo);
    }
}
