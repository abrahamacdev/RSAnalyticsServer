package controlador.managers.informes;

import modelo.pojo.scrapers.informe_inmueble.InformeInmueble;
import utilidades.Par;

import javax.persistence.EntityManager;

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
    // --------------

}
