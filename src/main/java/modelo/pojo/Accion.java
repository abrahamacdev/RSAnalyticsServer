package modelo.pojo;

import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "accion")
public class Accion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "completada", nullable = false)
    private boolean completada;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_id", nullable = false)
    private Tipo tipo;

    public Accion(){}

    public Accion(Grupo grupo, Tipo tipo) {
        this.grupo = grupo;
        this.tipo = tipo;
    }

    public Accion(boolean completada, Grupo grupo, Tipo tipo) {
        this.completada = completada;
        this.grupo = grupo;
        this.tipo = tipo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Accion accion = (Accion) o;
        return Objects.equals(id, accion.id);
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

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }
}
