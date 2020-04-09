package controlador.managers;

import modelo.pojo.Token;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.tinylog.Logger;
import utilidades.Par;
import utilidades.Utils;

public class ControladorTokens {


    // ----- Create -----
    public Par<Integer, Token> guardarNuevoToken(Token token){

        Session session = Utils.crearNuevaSesion();
        Transaction transaction = null;
        Par<Integer, Token> respuesta = null;

        try {

            transaction = session.beginTransaction();

            Integer id = (Integer) session.save(token);
            token.setId(id);

            transaction.commit();

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
