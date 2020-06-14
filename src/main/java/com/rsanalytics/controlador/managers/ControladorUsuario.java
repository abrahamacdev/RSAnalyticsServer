package com.rsanalytics.controlador.managers;

import com.rsanalytics.modelo.pojo.rest.Usuario;
import org.hibernate.Hibernate;
import org.tinylog.Logger;
import com.rsanalytics.utilidades.Par;
import com.rsanalytics.utilidades.rest.SecurityUtils;
import com.rsanalytics.utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.List;

public class ControladorUsuario {

    private SecurityUtils securityUtils;

    public ControladorUsuario(){
        this.securityUtils = new SecurityUtils();
    }

    // ----- Read -----
    /**
     * Buscaremos al usuario con el correo proporcionado
     * @param correo
     * @return 0, Usuario -> En caso de exista un usuario |
     *         1,null -> En caso de que halla un error |
     *         2,null -> En caso de que no exista el usuario
     */
    public Par<Integer, Usuario> buscarUsuarioPorCorreo(String correo){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction transaction = null;
        Par<Integer, Usuario> respuesta;

        try {

            transaction = entityManager.getTransaction();
            transaction.begin();

            Query query = entityManager.createQuery("FROM Usuario AS us WHERE us.correo = :correo");
            query.setParameter("correo", correo);

            List<Usuario> listaUsuarios = query.getResultList();

            transaction.commit();

            // No existe ningun usuario con ese correo
            if (listaUsuarios.size() == 0){
                respuesta = new Par<>(2, null);
            }

            // Exite un usuario con ese correo
            else {
                Usuario usuario = listaUsuarios.get(0);
                Hibernate.initialize(usuario.getRol());
                respuesta = new Par<>(0, usuario);
            }

        }catch (Exception e){
            Logger.error("Ocurrio un error al buscar a un usuario");
            respuesta = new Par<>(1, null);
        }finally {
            entityManager.close();
        }

        return respuesta;
    }
    // ----------------


    // ----- Create -----
    /**
     * Creamos un nuevo reegistro en la base de datos a partir del objeto usuario recibido
     * @param usuario
     * @return 0,Usuario -> En caso de que se halla podido crear el registro
     *         1,null -> En caso de que halla ocurrido un error
     */
    public Par<Integer, Usuario> guardarNuevoUsuario(Usuario usuario){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction transaction = null;
        Par<Integer, Usuario> respuesta = null;

        try {

            transaction = entityManager.getTransaction();
            transaction.begin();

            // Ciframos la contraseña antes de guardarla en el servidor
            Par<byte[], byte[]> contraseniaConSalt = securityUtils.cifrarContrasenia(usuario.getContrasenia());
            usuario.setContrasenia(contraseniaConSalt.getPrimero());
            usuario.setSalt(contraseniaConSalt.getSegundo());

            entityManager.persist(usuario);
            transaction.commit();

            usuario.getId();

            respuesta = new Par<>(0, usuario);

        }catch (Exception e){
            Logger.error(e, "Ocurrio un error al insertar un nuevo usuario");
            respuesta = new Par<>(1, null);
        }finally {
            entityManager.close();
        }

        return respuesta;

    }
    // ------------------

}
