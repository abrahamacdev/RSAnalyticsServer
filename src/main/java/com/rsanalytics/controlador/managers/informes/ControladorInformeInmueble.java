package com.rsanalytics.controlador.managers.informes;

import com.rsanalytics.modelo.pojo.scrapers.Informe;
import com.rsanalytics.modelo.pojo.scrapers.Inmueble;
import com.rsanalytics.modelo.pojo.scrapers.informe_inmueble.InformeInmueble;
import org.hibernate.Hibernate;
import com.rsanalytics.utilidades.Par;
import com.rsanalytics.utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.Query;

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
