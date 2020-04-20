package modelo.pojo;

import modelo.pojo.usuario_grupo.UsuarioGrupo;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "grupo")
public class Grupo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", unique = true)
    private String nombre;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinColumn(name = "responsable_id", referencedColumnName = "id")
    private Usuario responsable;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            mappedBy = "grupo" // Nombre de la variable en la clase "UsuarioGrupo.java"
    )
    private Set<UsuarioGrupo> usuarios = new HashSet<>(0);

    public Grupo(){}

    public Grupo(String nombre, Usuario responsable){
        this.nombre = nombre;
        this.responsable = responsable;
    }

    public Grupo(String nombre, Set<UsuarioGrupo> usuarios, Usuario responsable) {
        this.nombre = nombre;
        this.usuarios = usuarios;
        this.responsable = responsable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Set<UsuarioGrupo> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(Set<UsuarioGrupo> usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario getResponsable() {
        return responsable;
    }

    public void setResponsable(Usuario responsable) {
        this.responsable = responsable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grupo grupo = (Grupo) o;
        return Objects.equals(id, grupo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
