package com.rsanalytics.controlador.managers;

import com.rsanalytics.modelo.pojo.rest.Token;
import org.tinylog.Logger;
import com.rsanalytics.utilidades.Par;
import com.rsanalytics.utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;

public class ControladorToken {


    // ----- Create -----
    public Par<Integer, Token> guardarNuevoToken(Token token){

        EntityManager session = Utils.crearEntityManager();
        EntityTransaction transaction = null;
        Par<Integer, Token> respuesta = null;

        try {

            transaction = session.getTransaction();

            transaction.begin();
            session.persist(token);
            transaction.commit();

            token.getId();

            respuesta = new Par<>(0, token);

        }catch (Exception e){
            Logger.error("Ocurrio un error al insertar un nuevo token", e);
            respuesta = new Par<>(1, null);
        }finally {
            session.close();
        }

        return respuesta;
    }
    // -----------------


    // ----- Read -----
    /**
     * Buscamos un token en la base de datos a partir de su idPublico
     * @param idPublico
     * @return  0, null -> No se ha encontradoo ningun token con ese id
     *          0, token -> Token encontrado en la base de datos
     *          1, null -> Ocurrio otro error
     */
    public Par<Integer, Token> buscarTokenPorIdPublico(String idPublico){

        EntityManager session = Utils.crearEntityManager();
        EntityTransaction transaction = null;
        Par<Integer, Token> respuesta = null;

        try {

            transaction = session.getTransaction();

            transaction.begin();

            Query query = session.createQuery("FROM Token AS tok WHERE tok.idPublico = :idPublico");
            query.setParameter("idPublico", idPublico);
            query.setMaxResults(1);

            Token token = (Token) query.getSingleResult();

            transaction.commit();

            return new Par<>(0, token);

        }catch (NoResultException noResult){
            return new Par<>(0, null);
        } catch (Exception e){
            e.printStackTrace();
            return new Par<>(1, null);
        }finally {
            session.close();
        }
    }
    // ----------------

}
