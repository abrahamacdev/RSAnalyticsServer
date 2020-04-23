package modelo.pojo;

import org.json.simple.JSONObject;

import javax.persistence.*;
import java.nio.charset.Charset;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "primer_apellido")
    private String primerApellido;

    @Column(name = "segundo_apellido")
    private String segundoApellido;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "correo")
    private String correo;

    @Column(name = "contrasenia")
    private byte[] contrasenia;

    @Column(name = "salt")
    private byte[] salt;

    @Column(name = "genero")
    private String genero;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id")
    private Rol rol;



    public Usuario(){}

    public Usuario(String nombre, String primerApellido, String segundoApellido, String genero, String telefono, String correo, byte[] contrasenia) {
        this(nombre, primerApellido, segundoApellido, genero, telefono, correo, contrasenia, null, Rol.getRolPorDefecto());
    }



    public Usuario(String nombre, String primerApellido, String segundoApellido, String genero, String telefono, String correo, byte[] contrasenia, byte[] salt, Rol rol) {
        this.nombre = nombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.genero = genero;
        this.telefono = telefono;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.salt = salt;
        this.rol = rol;
    }

    public static Usuario fromJson(JSONObject jsonObject){

        String correo = null;
        String nombre = null;
        String primerAp = null;
        String segundoAp = null;
        String telefono = null;
        String genero = null;
        byte[] contrasenia = null;

        System.out.println(jsonObject);

        if (jsonObject.containsKey("correo")){
            correo = (String) jsonObject.get("correo");
        }

        if (jsonObject.containsKey("nombre")){
            nombre = (String) jsonObject.get("nombre");
        }

        if (jsonObject.containsKey("primerApellido")){
            primerAp = (String) jsonObject.get("primerApellido");
        }

        if (jsonObject.containsKey("segundoApellido")){
            segundoAp = (String) jsonObject.get("segundoApellido");
        }

        if (jsonObject.containsKey("genero")){
            genero = ((String) jsonObject.get("genero"));
        }

        if (jsonObject.containsKey("telefono")){
            telefono = (String) jsonObject.get("telefono");
        }

        if (jsonObject.containsKey("contrasenia")){
            contrasenia = ((String)jsonObject.get("contrasenia")).getBytes(Charset.forName("UTF-8"));
        }

        return new Usuario(nombre, primerAp, segundoAp, genero,telefono, correo, contrasenia);
    }

    @Override
    public String toString() {
        return nombre + " - " + primerApellido + " - " + segundoApellido + " - " + telefono + " - " + correo +
                ". Tiene contrasenia? - " + (contrasenia != null) + " - " + (rol == null ? "" : rol);
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

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public byte[] getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(byte[] contrasenia) {
        this.contrasenia = contrasenia;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }
}
