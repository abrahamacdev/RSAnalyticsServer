package controlador.managers;

import modelo.pojo.Grupo;
import modelo.pojo.Usuario;
import modelo.pojo.usuario_grupo.UsuarioGrupo;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.*;
import java.util.List;

public class ControladorGrupo {

    // ----- Create -----
    public Par<Exception,Grupo> guardarNuevoGrupo(Grupo grupo){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();
        Par<Exception, Grupo> resultado = guardarNuevoGrupo(grupo, entityManager);

        if (resultado.getPrimero() != null){
            transaction.rollback();
        }
        else {
            transaction.commit();
        }

        entityManager.close();

        return resultado;
    }

    public Par<Exception,Grupo> guardarNuevoGrupo(Grupo grupo, EntityManager entityManager){

        try {
            entityManager.merge(grupo);
            grupo.getId(); // Forzamos al objeto a cargar su id en memoria
        }catch (Exception e){
            e.printStackTrace();
            return new Par<>(e, null);
        }

        return new Par<>(null, grupo);
    }

    // ------------------


    // ----- Read -----
    public Par<Integer,Grupo> buscarGrupoPorNombre(String nombreGrupo){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();
        Par<Integer, Grupo> resultado = buscarGrupoPorNombre(nombreGrupo, entityManager);

        // Ocurrio un error
        if (resultado.getPrimero() != null){
            transaction.rollback();
            return resultado;
        }

        // No se ha encontrado ningun grupo
        else if (resultado.getSegundo() == null){
            transaction.rollback();
        }

        // Hemos encontrado un grupo con ese nombre
        else {
            transaction.commit();
        }

        entityManager.close();
        return resultado;
    }

    /**
     * Comprobamos si existe un grupoo a partir del nombre proporcionado
     * @param nombreGrupo
     * @param entityManager
     * @return  0,Grupo -> Grupo enncontrado en la base de datos
     *          1,null -> No se ha encontrado ningun grupo en la base de datos con ese nombre
     *          2,null -> Ocurrioo un error desconocido
     */
    public Par<Integer,Grupo> buscarGrupoPorNombre(String nombreGrupo, EntityManager entityManager){

        try {
            Query query = entityManager.createQuery("FROM Grupo AS gr WHERE nombre = :nombre");
            query.setParameter("nombre", nombreGrupo);
            query.setMaxResults(1);
            Grupo encontrado = (Grupo) query.getSingleResult();

            return new Par<>(0, encontrado);

        }catch (NoResultException noResult){
            return new Par<>(1,null);
        }
        catch (Exception e){
            return new Par<>(2, null);
        }
    }


    public Par<Integer,Grupo> buscarGrupoDelUsuarioConCorreo(String correo){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();
        Par<Integer, Grupo> resultado = buscarGrupoDelUsuarioConCorreo(correo, entityManager);

        // Ocurrio un error
        if (resultado.getPrimero() != null){
            transaction.rollback();
            return resultado;
        }

        // No se ha encontrado ningun grupo
        else if (resultado.getSegundo() == null){
            transaction.rollback();
        }

        // Hemos encontrado un grupo con ese nombre
        else {
            transaction.commit();
        }

        entityManager.close();
        return resultado;

    }

    /**
     * Obtenemmos el grupo al que pertenece un usuario a partir del correo pasado
     * por parametros
     * @param correo
     * @param entityManager
     * @return  0,Grupo -> Grupo al que pertenece el usuario
     *          1,null -> No tiene ningun grupo asociado
     *          2,null -> EL usuario pertenece a mas de un grupo
     *          3,null -> Ocurrio un error desconocido
     */
    public Par<Integer,Grupo> buscarGrupoDelUsuarioConCorreo(String correo, EntityManager entityManager){

        try {
            Query query = entityManager.createQuery("FROM UsuarioGrupo AS gr WHERE gr.usuario.correo = :correo");
            query.setParameter("correo", correo);
            query.setMaxResults(1);
            Grupo encontrado = ((UsuarioGrupo) query.getSingleResult()).getGrupo();

            return new Par<>(0, encontrado);

        }catch (NoResultException noResult){
            return new Par<>(1,null);
        }catch (NonUniqueResultException noUnico){
            return new Par<>(2, null);
        }
        catch (Exception e){
            e.printStackTrace();
            return new Par<>(3, null);
        }
    }


    public int elUsuarioEsResponsableDeGrupo(String correo){


        EntityManager entityManager = Utils.crearEntityManager();

        int resultado = elUsuarioEsResponsableDeGrupo(correo, entityManager);

        entityManager.close();
        return resultado;
    }

    /**
     * Comprobamos si el usuario con el correo pasado es responsable de algun grupo
     * @param correo
     * @param entityManager
     * @return  0 -> El usuario es responsable de un grupo
     *          1 -> El usuario no es responsable de ningun grupo
     *          2 -> EL usuario es responsable de mas de un grupo
     *          3 -> Ocurrio un error desconocido
     */
    public int elUsuarioEsResponsableDeGrupo(String correo, EntityManager entityManager){

        try{

            Query query = entityManager.createQuery("FROM Grupo AS gr WHERE gr.responsable.correo = :correo");
            query.setParameter("correo", correo);

            List<Object> grupos = query.getResultList();

            switch (grupos.size()){

                // No es responsable de ningun grupo
                case 0:
                    return 1;

                // Es responsable de un grupo
                case 1:
                    return 0;

                //  Es responsable de multiples grupos
                default:
                    return 2;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return 3;
        }
    }
    // ----------------


    // ----- Update -----
    public int actualizarGrupo(Grupo grupo){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();
        int resultado = actualizarGrupo(grupo, entityManager);

        if (resultado == 1){
            transaction.rollback();
        }
        else {
            transaction.commit();
        }

        entityManager.close();

        return resultado;
    }

    public int actualizarGrupo(Grupo grupo, EntityManager entityManager){

        try {
            entityManager.merge(grupo);
            return 0;

        }catch (Exception e){
            return -1;
        }
    }
    // ------------------


    // ----- Delete -----
    public int eliminarDelGrupoAlUsuario(String correo){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();
        int resultado = eliminarDelGrupoAlUsuario(correo, entityManager);

        // Ocurrio un error
        if (resultado == 3){
            transaction.rollback();
        }

        // Se realizo alguna eliminacion o el usuario no pertenecia a ningun grupo
        else {
            transaction.commit();
        }

        entityManager.close();
        return resultado;

    }

    /**
     * Eliminamos al usuario del grupo al que pertenezca, en caso de que el usuario sea el responsable del grupo
     * se eliminara el grupo por completo
     * @param correo
     * @param entityManager
     * @return  0 -> Se ha eliminado el miembro del grupo
     *          1 -> Se ha elliminado el grupo por completo
     *          2 -> El usuario no pertenecia a ningun grupo
     *          3 -> Ocurrio un error desconocido
     */
    public int eliminarDelGrupoAlUsuario(String correo, EntityManager entityManager){

        boolean esResponsableDeGrupo = elUsuarioEsResponsableDeGrupo(correo, entityManager) == 0;
        //boolean esResponsableDeGrupo = false;

        try{

            // Obtenemos el grupo del que es responsable y lo eliminamos
            if (esResponsableDeGrupo){
                Query queryGrupo = entityManager.createQuery("FROM Grupo AS gr WHERE gr.responsable.correo = :correo");
                queryGrupo.setParameter("correo", correo);
                queryGrupo.setMaxResults(1);

                // Obtenemos el grupo al que pertenece y lo eliminamos por completo
                Grupo grupo = (Grupo) queryGrupo.getSingleResult();

                entityManager.remove(grupo);

                return 1;
            }

            else {

                Query queryGrupoQuePertenece = entityManager.createQuery("FROM UsuarioGrupo AS usGr WHERE usGr.usuario.correo = :correo");
                queryGrupoQuePertenece.setParameter("correo", correo);
                queryGrupoQuePertenece.setMaxResults(1);


                // Obtenemos el grupo al que pertenece y le expulsamos de este
                UsuarioGrupo usuarioGrupo = (UsuarioGrupo) queryGrupoQuePertenece.getSingleResult();

                entityManager.remove(usuarioGrupo);

                return 0;
            }

        } catch (NoResultException noResult){
            return 2;
        } catch (Exception e){
            return 3;
        }
    }
    // ------------------
}
