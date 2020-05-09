package controlador.managers.anuncios;

import modelo.pojo.scrapers.ClaveAtributoAnuncio;
import org.hibernate.Session;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.List;

public class ControladorAtributoAnuncio {

    // ----- Read -----
    /**
     * Obtenemos todas las posibles claves de los atributos
     * @return  List<ClaveAtributoAnuncio> -> Listas de claves
     *          null -> Ocurrio un error desconocido
     */
    public List<ClaveAtributoAnuncio> obtenerClavesPosibles(){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        List<ClaveAtributoAnuncio> claveAtributoAnuncios = null;

        entityTransaction.begin();

        try {

            Query query = entityManager.createQuery("FROM ClaveAtributoAnuncio");
            claveAtributoAnuncios = query.getResultList();

        }catch (Exception e){

        }

        entityTransaction.commit();
        entityManager.close();

        return claveAtributoAnuncios;
    }

    public ClaveAtributoAnuncio obtenerClaveConNombre(String nombre){

        EntityManager entityManager = Utils.crearEntityManager();

        ClaveAtributoAnuncio res = null;

        try {

            Query query = entityManager.createQuery("FROM ClaveAtributoAnuncio AS cla WHERE cla.nombre = :nombre");
            query.setParameter("nombre", nombre);

            res = (ClaveAtributoAnuncio) query.getSingleResult();

        } catch (Exception e){

        }

        entityManager.close();

        return res;
    }
    // ----------------
    
    // ----- Update -----
    public int actualizarClaves(List<ClaveAtributoAnuncio> clavesAnuncios, EntityManager entityManager){

        int actualizados = 0;

        Session session = entityManager.unwrap(Session.class);

        for (ClaveAtributoAnuncio claveAnuncio : clavesAnuncios) {

            try {

                session.saveOrUpdate(claveAnuncio);
                actualizados++;

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return actualizados;
    }
    // ------------------
}
