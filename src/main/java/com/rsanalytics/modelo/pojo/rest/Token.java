package com.rsanalytics.modelo.pojo.rest;

import javax.persistence.*;

@Entity(name = "Token")
@Table(name = "tokenAcceso")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "publico_id")
    private String idPublico;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Token(){}

    public Token(String idPublico, Usuario usuario){
        this.idPublico = idPublico;
        this.usuario = usuario;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdPublico() {
        return idPublico;
    }

    public void setIdPublico(String idPublico) {
        this.idPublico = idPublico;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
