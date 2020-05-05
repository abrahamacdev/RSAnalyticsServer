package controlador.managers;

import modelo.pojo.rest.Notificacion;
import modelo.pojo.rest.Tipo;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class ControladorNotificacion {

    // ----- Create -----
    public int guardarNuevaNotificacion(Notificacion notificacion){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();

        int resultado = guardarNuevaNotificacion(notificacion, entityManager);

        if (resultado != 0){
            entityTransaction.rollback();
        }

        else {
            entityTransaction.commit();
        }

        entityManager.close();

        return resultado;
    }

    /**
     * Guardamos en la base de datos una nueva notificacion
     * @param notificacion
     * @return  0 -> Se guardo exitosamente
     *          1 -> Algo salio mal
     */
    public int guardarNuevaNotificacion(Notificacion notificacion, EntityManager entityManager){

        try {
            entityManager.persist(notificacion);

            notificacion.getId();

            return 0;

        }catch (Exception e){
            e.printStackTrace();
            return 1;
        }
    }
    // ------------------


    // ----- Read -----
    public Par<Integer, List<Notificacion>> obtenerTodasNotificaciones(String correo){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();

        Par<Integer, List<Notificacion>> resultado = obtenerTodasNotificaciones(correo, entityManager);

        entityTransaction.commit();
        entityManager.close();

        return resultado;
    }

    /**
     * Obtenemos todas las notificaciones disponibles del usuario
     * @param correo
     * @param entityManager
     * @return  0, List -> Listado de notificaciones del usuario
     *          1, null -> El usuario no tiene notificaciones
     *          2, null -> Ocurrio un error
     */
    public Par<Integer, List<Notificacion>> obtenerTodasNotificaciones(String correo, EntityManager entityManager){

        try {

            Query query = entityManager.createQuery("FROM Notificacion AS noti WHERE noti.receptor.correo = :correo ORDER BY noti.fechaEnvio", Notificacion.class);
            query.setParameter("correo", correo);

            List<Notificacion> notificaciones = query.getResultList();

            if (notificaciones.size() > 0){
                return new Par<>(0, notificaciones);
            }

            return new Par<>(1,null);

        }catch (Exception e){
            return new Par<>(2, null);
        }
    }


    public Par<Integer,List<Notificacion>> obtenerNotificacionesNoLeidas(String correo){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();

        Par<Integer, List<Notificacion>> resultado = obtenerNotificacionesNoLeidas(correo, entityManager);

        entityTransaction.commit();
        entityManager.close();

        return resultado;
    }

    /**
     * Obtenemos todas las notificaciones no leidas del usuario
     * @param correo
     * @param entityManager
     * @return  0, List -> Listado de notificaciones no leidas del usuario
     *          1, null -> El usuario no tiene notificaciones no leidas
     *          2, null -> Ocurrio un error
     */
    public Par<Integer,List<Notificacion>> obtenerNotificacionesNoLeidas(String correo, EntityManager entityManager){

        try {

            Query query = entityManager.createQuery("FROM Notificacion AS noti WHERE noti.receptor.correo = :correo AND leida = FALSE", Notificacion.class);
            query.setParameter("correo", correo);

            List<Notificacion> notificaciones = query.getResultList();

            if (notificaciones.size() > 0){
                return new Par<>(0, notificaciones);
            }

            return new Par<>(1,null);

        }catch (Exception e){
            return new Par<>(2, null);
        }
    }


    public int tieneNotificacionesNoLeidas(String correo){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();

        int resultado = tieneNotificacionesNoLeidas(correo, entityManager);

        entityTransaction.commit();
        entityManager.close();

        return resultado;
    }

    /**
     * Coprobamoos si el usuario tiene notificaciones noo leidas
     * @param correo
     * @param entityManager
     * @return  0 -> Tiene notificaciones no leidas
     *          1 -> NO tiene notificaciones no leidas
     *          2 -> Ocurrio un error
     */
    public int tieneNotificacionesNoLeidas(String correo, EntityManager entityManager){

        try {

            Query query = entityManager.createQuery("FROM Notificacion AS noti WHERE noti.receptor.correo = :correo AND noti.leida = FALSE", Notificacion.class);
            query.setParameter("correo", correo);
            query.setMaxResults(1);

            Notificacion notificacion = (Notificacion) query.getSingleResult();

            return 0;
        }catch (NoResultException nooResult){
            return 1;
        }
        catch (Exception e){
            return 2;
        }
    }


    public Par<Integer, List<Notificacion>> obtenerInvitacionesGrupoDelUsuario(String correo){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();

        Par<Integer, List<Notificacion>> resultado = obtenerInvitacionesGrupoDelUsuario(correo, entityManager);

        entityTransaction.commit();
        entityManager.close();

        return resultado;
    }

    /**
     * Obtenemos todas las invitaciones a grupo que ha recibido un usuario
     * @param correo
     * @param entityManager
     * @return  0, List -> Listado con las invitaciones a grupos
     *          1, null -> NO ha recibido ninguna invitacion a grupo
     *          2, null -> Ocurrio un error
     */
    public Par<Integer, List<Notificacion>> obtenerInvitacionesGrupoDelUsuario(String correo, EntityManager entityManager){

        try {

            String sentencia = "FROM Notificacion AS noti WHERE noti.receptor.correo = :correo AND noti.accion.tipo.id = " + Tipo.NOMBRE.INVITACION.getId();

            Query query = entityManager.createQuery(sentencia, Notificacion.class);
            query.setParameter("correo", correo);

            List<Notificacion> notificaciones = query.getResultList();

            if (notificaciones.size() > 0){
                return new Par<>(0, notificaciones);
            }

            return new Par<>(1, null);

        }
        catch (Exception e){
            e.printStackTrace();
            return new Par<>(2, null);
        }

    }


    public Par<Integer, Notificacion> obtenerNotificacionConId(int id){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();

        Par<Integer, Notificacion> resultado = obtenerNotificacionConId(id, entityManager);

        entityTransaction.commit();
        entityManager.close();

        return resultado;
    }

    /**
     * Obtenemos la notificacion que coincida con el id pasado por parametros
     * @param id
     * @param entityManager
     * @return  0, Notificacion -> Notificacion buscada
     *          1, null -> No existe una notificacion con ese id
     *          2, null -> Ocurrio un error
     */
    public Par<Integer, Notificacion> obtenerNotificacionConId(int id, EntityManager entityManager){

        try {

            String sentencia = "FROM Notificacion AS noti WHERE noti.id = :id";

            Query query = entityManager.createQuery(sentencia, Notificacion.class);
            query.setParameter("id", id);

            Notificacion notificacion = (Notificacion) query.getSingleResult();

            return new Par<>(0, notificacion);

        }catch (NoResultException noResult){
            return new Par<>(1,null);
        }
        catch (Exception e){
            return new Par<>(2, null);
        }

    }


    public Par<Integer, List<Notificacion>> obtenerNotificacionesConIdDelUsuario(List<Integer> ids, String correo){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();

        Par<Integer, List<Notificacion>> resultado = obtenerNotificacionesConIdDelUsuario(ids, correo, entityManager);

        entityTransaction.commit();
        entityManager.close();

        return resultado;
    }

    /**
     * Devolvemos las notificacionnes que coincidan con los ids pasados poor parametros
     * @param ids
     * @param entityManager
     * @return  0, List -> Notificaciones que coincidieron
     *          1, List -> No hubo ninguna que coincidiese
     *          2, null -> Ocurrio un error
     */
    public Par<Integer, List<Notificacion>> obtenerNotificacionesConIdDelUsuario(List<Integer> ids, String correo, EntityManager entityManager){

        try {

            String sentencia = "FROM Notificacion AS noti WHERE noti.id IN (:ids) AND noti.receptor.correo = :correo";

            Query query = entityManager.createQuery(sentencia, Notificacion.class);
            query.setParameter("ids", ids);
            query.setParameter("correo", correo);

            List<Notificacion> notificaciones = query.getResultList();

            if (notificaciones.size() > 0){
                return new Par<>(0, notificaciones);
            }

            return new Par<>(1, notificaciones);

        }catch (Exception e){
            return new Par<>(2, null);
        }
    }
    // ----------------


    // ----- Update -----
    public int actualizarNotificacion(Notificacion notificacion){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();
        int resultado = actualizarNotificacion(notificacion, entityManager);

        if (resultado == 1){
            transaction.rollback();
        }
        else {
            transaction.commit();
        }

        entityManager.close();

        return resultado;

    }

    /**
     * Actualizamos la notificacion pasada por parametros
     * @param notificacion
     * @return  0 -> Se actualizo la notificacion correctammente
     *          1 -> Algo salio mal
     */
    public int actualizarNotificacion(Notificacion notificacion, EntityManager entityManager){

        try {
            entityManager.merge(notificacion);

            return 0;

        }catch (Exception e){
            return 1;
        }
    }
    // ------------------
}
