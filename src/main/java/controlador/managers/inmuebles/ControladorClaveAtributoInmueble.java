package controlador.managers.inmuebles;

import modelo.pojo.scrapers.ClaveAtributoInmueble;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class ControladorClaveAtributoInmueble {

    // --- Read ---
    public Par<Exception, ClaveAtributoInmueble> buscarClaveAtributoInmuebleConNombre(String nombre){

        EntityManager entityManager = Utils.crearEntityManager();

        Par<Exception, ClaveAtributoInmueble> resBus = buscarClaveAtributoInmuebleConNombre(nombre, entityManager);

        entityManager.close();

        return resBus;
    }

    public Par<Exception, ClaveAtributoInmueble> buscarClaveAtributoInmuebleConNombre(String nombre, EntityManager entityManager){

        try {

            Query query = entityManager.createQuery("FROM ClaveAtributoInmueble AS clavAtIn WHERE clavAtIn.nombre = :nombre");
            query.setParameter("nombre", nombre);

            return new Par<>(null, (ClaveAtributoInmueble) query.getSingleResult());

        }catch (Exception e){
            return new Par<>(e, null);
        }
    }
    // ------------
}
