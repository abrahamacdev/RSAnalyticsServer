package controlador.managers;

import modelo.pojo.Grupo;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;

public class ControladorGrupo {

    /* ----- Create ----- */
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
            entityManager.persist(grupo);
        }catch (Exception e){
            return new Par<>(e, null);
        }

        return new Par<>(null, grupo);
    }
    /* ------------------ */


    /* ----- Read ----- */
    public Par<Exception,Grupo> buscarGrupoPorNombre(String nombreGrupo){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        transaction.begin();
        Par<Exception, Grupo> resultado = buscarGrupoPorNombre(nombreGrupo, entityManager);

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

    public Par<Exception,Grupo> buscarGrupoPorNombre(String nombreGrupo, EntityManager entityManager){

        try {
            Query query = entityManager.createQuery("FROM Grupo AS gr WHERE nombre = :nombre");
            query.setParameter("nombre", nombreGrupo);
            query.setMaxResults(1);
            Grupo encontrado = (Grupo) query.getSingleResult();

            return new Par<>(null, encontrado);

        }catch (NoResultException noResult){
            return new Par<>(null,null);
        }
        catch (Exception e){
            return new Par<>(e, null);
        }
    }
    /* ---------------- */


}
