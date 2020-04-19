package controlador.managers;

import modelo.pojo.Token;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.tinylog.Logger;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class ControladorTokens {


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

}
