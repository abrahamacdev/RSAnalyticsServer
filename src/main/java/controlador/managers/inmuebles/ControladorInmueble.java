package controlador.managers.inmuebles;

import modelo.pojo.Municipio;
import modelo.pojo.rest.Tipo;
import modelo.pojo.scrapers.ClaveAtributoInmueble;
import modelo.pojo.scrapers.Inmueble;
import modelo.pojo.scrapers.TipoContrato;
import modelo.pojo.scrapers.anuncio_inmueble_tipoContrato.AnuncioInmuebleTipoContrato;
import modelo.pojo.scrapers.atributo_inmueble.AtributoInmueble;
import org.tinylog.Logger;
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
    private ControladorClaveAtributoInmueble controladorClaveAtributoInmueble = new ControladorClaveAtributoInmueble();

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

    // ----- Read -----
    public Par<Exception, List<Inmueble>> buscarInmuebles(TipoContrato tipoContrato, Municipio municipio, HashMap<Integer, Object> datos){

        EntityManager entityManager = Utils.crearEntityManager();

        Par<Exception, List<Inmueble>> res = buscarInmuebles(tipoContrato,municipio, datos, entityManager);

        entityManager.close();

        return res;
    }

    public Par<Exception, List<Inmueble>> buscarInmuebles(TipoContrato tipoContrato, Municipio municipio, HashMap<Integer, Object> datos, EntityManager entityManager){

        try {

            Par<Exception, ClaveAtributoInmueble> resBusClaveTipInm = controladorClaveAtributoInmueble.buscarClaveAtributoInmuebleConNombre("Id Tipo Inmueble");
            if (resBusClaveTipInm.getPrimero() != null) {
                return new Par<>(new Exception("Ocurrio un error"), null);
            }

            Par<Exception, List<Integer>> reesBusInms = controladorAtributoInmueble.obtenerInmueblesConAtributo(datos);
            if (reesBusInms.getPrimero() != null){
                return new Par<>(new Exception("Ocurrioo un error"), null);
            }

            // Obtenemos los ids de los inmuebles que sean del tipo seleccionado y que
            // pertenezcan al municipio elegido
            Query query = entityManager.createNativeQuery("SELECT inm.id " +
                    "FROM inmueble inm " +
                    "WHERE inm.id IN :idsInmuebles " +
                    "AND inm.municipio_id = :idMunicipio");
            query.setParameter("idsInmuebles", reesBusInms.getSegundo());
            query.setParameter("idMunicipio", municipio.getId());

            List<Integer> idsInmuebles = query.getResultList();

            // De la lista anterior obtendremos solo aquellos con el tipo de contrato solicitado
            query = entityManager.createNativeQuery("SELECT anTipIn.inmueble_id\n" +
                    "FROM anuncio_tipoContrato_inmueble anTipIn\n" +
                    "WHERE anTipIn.inmueble_id IN :idsInmuebles\n" +
                    "AND anTipIn.tipoContrato_id = :idTipCont");
            query.setParameter("idsInmuebles", idsInmuebles);
            query.setParameter("idTipCont", tipoContrato.getId());

            idsInmuebles = query.getResultList();

            // Obtenemos los objetos "inmueble" finales
            query = entityManager.createQuery("FROM Inmueble AS inm WHERE inm.id IN :ids");
            query.setParameter("ids", idsInmuebles);

            return new Par<>(null, (List<Inmueble>) query.getResultList());

        }catch (Exception e){
            return new Par<>(e, null);
        }
    }
    // ----------------

}
