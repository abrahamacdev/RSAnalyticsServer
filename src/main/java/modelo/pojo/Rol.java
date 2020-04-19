package modelo.pojo;

import org.tinylog.Logger;
import utilidades.Utils;

import javax.persistence.*;

@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre")
    private String nombre;



    public Rol(){}

    public Rol(String nombre){
        this.nombre = nombre;
    }

    public static synchronized Rol getRolPorDefecto(){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction transaction;

        try{

            transaction = entityManager.getTransaction();
            transaction.begin();

            Query query = entityManager.createQuery("FROM Rol AS rol WHERE rol.id = 2");
            Rol rol = (Rol) query.getResultList().get(0);

            transaction.commit();

            return rol;
        }catch (Exception e){
            Logger.error("Ocurrio un error al obtener el rol por defecto");
        }
        finally {
            entityManager.close();
        }

        return null;
    }

    public boolean esAdmin(){
        return this.id == 1;
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
