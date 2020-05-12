package controlador.managers.inmuebles;

import modelo.pojo.scrapers.ClaveAtributoAnuncio;
import modelo.pojo.scrapers.ClaveAtributoInmueble;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class ControladorAtributoInmueble {

    // ----- Read -----
    /**
     * Obtenemos todas las posibles claves de los atributos
     * @return  null, List<ClaveAtributoInmueble> -> Listas de claves
     *          Exception, null -> Ocurrio un error desconocido
     */
    public Par<Exception, List<ClaveAtributoInmueble>> obtenerClavesPosibles(){

        EntityManager entityManager = Utils.crearEntityManager();
        Par<Exception, List<ClaveAtributoInmueble>> res;

        try {

            Query query = entityManager.createQuery("FROM ClaveAtributoInmueble");
            List<ClaveAtributoInmueble> claves = query.getResultList();

            res = new Par<>(null, claves);

        }catch (Exception e){
            res = new Par<>(e, null);
        }

        entityManager.close();

        return res;
    }

    public Par<Exception, ClaveAtributoInmueble> obtenerClaveConNombre(String nombre){

        EntityManager entityManager = Utils.crearEntityManager();
        Par<Exception, ClaveAtributoInmueble> res = null;

        try {

            Query query = entityManager.createQuery("FROM ClaveAtributoInmueble AS cla WHERE cla.nombre = :nombre");
            query.setParameter("nombre", nombre);

            res = new Par<>(null, (ClaveAtributoInmueble) query.getSingleResult());

        } catch (Exception e){
            res =  new Par<>(e, null);
        }

        entityManager.close();

        return res;
    }

    public Par<Exception, List<ClaveAtributoInmueble>> obtenerClavesConNombres(List<String> nombres, EntityManager entityManager){

        Par<Exception, List<ClaveAtributoInmueble>> res = null;

        try {

            Query query = entityManager.createQuery("FROM ClaveAtributoInmueble AS cla WHERE cla.nombre IN :nombres");
            query.setParameter("nombres", nombres);

            res = new Par(null, (List<ClaveAtributoInmueble>) query.getResultList());

        } catch (Exception e){
            res =  new Par<>(e, null);
        }

        return res;
    }
    // ----------------

}
