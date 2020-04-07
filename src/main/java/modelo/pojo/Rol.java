package modelo.pojo;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.tinylog.Logger;
import utilidades.Utils;

public class Rol {

    private int id;
    private String nombre;

    public Rol(){}

    public Rol(String nombre){
        this.nombre = nombre;
    }

    public static synchronized Rol getRolPorDefecto(){

        Session session = Utils.crearNuevaSesion();
        Transaction transaction;

        try{

            transaction = session.beginTransaction();

            Query query = session.createQuery("FROM Rol AS rol WHERE rol.id = 2");
            Rol rol = (Rol) query.getResultList().get(0);

            transaction.commit();

            return rol;
        }catch (Exception e){
            Logger.error("Ocurrio un error al obtener el rol por defecto");
        }

        return null;
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
}
