package controlador.managers.informes;

import modelo.pojo.scrapers.Informe;
import modelo.pojo.scrapers.Inmueble;
import modelo.pojo.scrapers.informe_inmueble.InformeInmueble;
import utilidades.Par;

import javax.persistence.EntityManager;
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
}
