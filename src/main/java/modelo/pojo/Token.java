package modelo.pojo;

public class Token {

    private int id;
    private String idPublico;
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
