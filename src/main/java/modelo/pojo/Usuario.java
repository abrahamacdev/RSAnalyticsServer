package modelo.pojo;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.json.simple.JSONObject;
import org.tinylog.Logger;
import utilidades.Utils;

import java.nio.charset.Charset;

public class Usuario {

    private int id;
    private String nombre;
    private String primerApellido, segundoApellido;
    private String telefono;
    private String correo;
    private byte[] contrasenia;
    private byte[] salt;
    private Rol rol;

    public Usuario(){}

    public Usuario(String nombre, String primerApellido, String segundoApellido, String telefono, String correo, byte[] contrasenia) {
        this(nombre, primerApellido, segundoApellido, telefono, correo, contrasenia, null, Rol.getRolPorDefecto());
    }



    public Usuario(String nombre, String primerApellido, String segundoApellido, String telefono, String correo, byte[] contrasenia, byte[] salt, Rol rol) {
        this.nombre = nombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
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
        byte[] contrasenia = null;

        Logger.info(jsonObject.toJSONString());

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

        if (jsonObject.containsKey("telefono")){
            telefono = (String) jsonObject.get("telefono");
        }

        if (jsonObject.containsKey("contrasenia")){
            contrasenia = ((String)jsonObject.get("contrasenia")).getBytes(Charset.forName("UTF-8"));
        }

        return new Usuario(nombre, primerAp, segundoAp, telefono, correo, contrasenia);
    }

    @Override
    public String toString() {
        return nombre + " - " + primerApellido + " - " + segundoApellido + " - " + telefono + " - " + correo;
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
