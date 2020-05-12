package controlador.managers.inmuebles;

import modelo.pojo.scrapers.ClaveAtributoInmueble;
import modelo.pojo.scrapers.Inmueble;
import modelo.pojo.scrapers.anuncio_inmueble_tipoContrato.AnuncioInmuebleTipoContrato;
import modelo.pojo.scrapers.atributo_inmueble.AtributoInmueble;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ControladorInmueble {

    private ControladorAtributoInmueble controladorAtributoInmueble = new ControladorAtributoInmueble();

    // ----- Create -----
    public Par<Exception,Inmueble> guardarInmueble(Inmueble inmueble, EntityManager entityManager){
        try {

            Map<String, AtributoInmueble> nombres = inmueble.getAtributos()
                                                            .stream()
                                                            .collect(Collectors.toMap(at -> at.getClaveAtributoInmueble().getNombre(), t -> t));

            Par<Exception, List<ClaveAtributoInmueble>> resBusAttsInm = controladorAtributoInmueble.obtenerClavesConNombres(new ArrayList<>(nombres.keySet()), entityManager);

            if (resBusAttsInm.getPrimero() != null){
                return new Par<>(resBusAttsInm.getPrimero(), null);
            }

            Map<String, ClaveAtributoInmueble> attsInmueble = resBusAttsInm.getSegundo()
                                                                    .stream()
                                                                    .collect(Collectors.toMap(at -> at.getNombre(), at -> at));

            // Refrescamos la instancia de "ClaveAtributoInmueble
            for (String k : attsInmueble.keySet()){
                nombres.get(k).setClaveAtributoInmueble(attsInmueble.get(k));
            }


            entityManager.persist(inmueble);

        }catch (Exception e){
            return new Par<>(e, null);
        }

        return new Par<>(null, inmueble);
    }
    // ----------------

}
