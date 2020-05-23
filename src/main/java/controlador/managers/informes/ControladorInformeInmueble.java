package controlador.managers.informes;

import modelo.pojo.scrapers.Informe;
import modelo.pojo.scrapers.Inmueble;
import modelo.pojo.scrapers.informe_inmueble.InformeInmueble;
import org.bouncycastle.LICENSE;
import org.hibernate.Hibernate;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class ControladorInformeInmueble {

    // --- Create ---
    public Par<Exception, InformeInmueble> guardarInformeInmueble(InformeInmueble informeInmueble, EntityManager entityManager){

        try {

            entityManager.persist(informeInmueble);
            return new Par<>(null, informeInmueble);

        }catch (Exception e){
            return new Par<>(e, null);
        }

    }
    // -------------

    // --- Read ---
    public Par<Exception, Inmueble> obtenerPrimerInmuebleDelInforme(Informe informe){

        EntityManager entityManager = Utils.crearEntityManager();

        Par<Exception, Inmueble> res = obtenerPrimerInmuebleDelInforme(informe, entityManager);

        entityManager.close();

        return res;
    }

    public Par<Exception, Inmueble> obtenerPrimerInmuebleDelInforme(Informe informe, EntityManager entityManager){

        try {

            Query query = entityManager.createQuery("FROM InformeInmueble AS infInm WHERE infInm.informe.id = :idInforme");
            query.setParameter("idInforme", informe.getId());
            query.setMaxResults(1);

            Inmueble inmueble = ((InformeInmueble)query.getSingleResult()).getInmueble();
            Hibernate.initialize(inmueble);

            return new Par(null, inmueble);

        }catch (Exception e){
            e.printStackTrace();
            return new Par<>(e, null);
        }

    }
}
