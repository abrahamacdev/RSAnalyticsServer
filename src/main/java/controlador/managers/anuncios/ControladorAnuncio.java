package controlador.managers.anuncios;

import controlador.managers.ControladorMunicipio;
import modelo.pojo.Municipio;
import modelo.pojo.scrapers.Anuncio;
import modelo.pojo.scrapers.ClaveAtributoAnuncio;
import utilidades.Constantes;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;

public class ControladorAnuncio {

    private ControladorMunicipio controladorMunicipio = new ControladorMunicipio();

    // ------ Create -----
    public int guardarAnuncios(List<Anuncio> anuncios){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        // Obtenemos una instancia "fresca" de los objetos "ClaveAtributo"
        ControladorAtributoAnuncio controladorAtributoAnuncio = new ControladorAtributoAnuncio();
        HashSet nombresClaves = new HashSet();
        ArrayList<ClaveAtributoAnuncio> clavesSinRepetir = new ArrayList<>();
        anuncios.stream()
                .map(anuncio -> anuncio.getAtributos())
                .flatMap(atributos -> atributos.stream())
                .map(atributoAnuncio -> atributoAnuncio.getClaveAtributoAnuncio())
                .filter(claveAtributoAnuncio -> !nombresClaves.contains(claveAtributoAnuncio.getNombre()))
                .forEach(claveAtributoAnuncio -> {
                    nombresClaves.add(claveAtributoAnuncio.getNombre());
                    clavesSinRepetir.add(claveAtributoAnuncio);
                });
        controladorAtributoAnuncio.actualizarClaves(clavesSinRepetir, entityManager);

        // Guardamos/Actualizamos los municipios para tener una instancia cacheada por hibernate y asi evitar
        // que nos de errores
        Set<Municipio> municipiosUnicos = new HashSet<>();
        anuncios.stream()
                .map(anuncio -> anuncio.getMunicipio())
                .filter(municipio -> !municipiosUnicos.contains(municipio))
                .forEach(municipio -> municipiosUnicos.add(municipio));
        List<Municipio> municipiosActualizados = controladorMunicipio.guardarOActualizarMunicipios(new ArrayList<>(municipiosUnicos), entityManager);
        HashMap<String, Municipio> munPorNombreCP = new HashMap<>();
        municipiosActualizados.stream()
                .forEach(mun -> munPorNombreCP.put(mun.getNombre(), mun));

        // Asignamos a cada anuncio el objeto municipio que acabamos de actualizar, asi nos aseguramos de
        // que todos los anuncios hagan referencia al objeto municipio accesible por Hibernate
        List<Anuncio> anunciosParaActualizar = anuncios.stream()
                .filter(anuncio -> {
                    return munPorNombreCP.containsKey(anuncio.getMunicipio().getNombre());
                })
                .map(anuncio -> {
                    // Seteamos la provincia recien guardada/actualizada
                    anuncio.setMunicipio(munPorNombreCP.get(anuncio.getMunicipio().getNombre()));
                    return anuncio;
                })
                .collect(Collectors.toList());

        List<Anuncio> anunciosActualizados = new ArrayList<>(anunciosParaActualizar.size());

        entityTransaction.begin();

        int guardados = 0;
        int batch = 0;
        for (Anuncio anuncio : anunciosParaActualizar){

            // Cada cierto tiempo hacemos un flush
            if (batch == Constantes.TAMANIO_BATCH_HIBERNATE){
                entityManager.flush();
                entityManager.clear();
                batch = 0;
            }

            // Guardamos el anuncio
            Par<Exception, Anuncio> res = guardarAnuncio(anuncio, entityManager);
            if (res.getSegundo() != null){
                guardados++;
            }

            batch++;
        }

        entityTransaction.commit();
        entityManager.close();

        return guardados;
    }

    /**
     * Guardamos un anuncio en la base de datos
     * @param anuncio
     * @param entityManager
     * @return  null, anuncio -> Se guardo el anuncio correctamente
     *          exception, null -> Algo salio mal
     */
    public Par<Exception, Anuncio> guardarAnuncio(Anuncio anuncio, EntityManager entityManager){

        try {

            entityManager.persist(anuncio);
            anuncio.getId();

            return new Par<>(null, anuncio);

        }catch (Exception e){
            e.printStackTrace();
            return new Par<>(e, null);
        }
    }
    // -------------------


    // ------ Read -----
    /**
     * Obteneemos todos los anuncios cuyo id se encuentre en la lista pasada por parametros
     * @param ids
     * @return  null, List<Anuncio> -> Listado de anuncios encontrados
     *          Exception, null -> Ocurrio un error
     */
    public Par<Exception, List<Anuncio>> obtenerAnunciosConId(List<Integer> ids){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        Par<Exception, List<Anuncio>> res;
        try {

            Query query = entityManager.createQuery("FROM Anuncio AS an WHERE an.id IN :ids");
            query.setParameter("ids", ids);
            List<Anuncio> anuncios  = query.getResultList();

            res = new Par<>(null, anuncios);

        } catch (Exception e){
            res = new Par<>(e, null);
        }

        entityManager.close();

        return res;
    }

    public Par<Exception, List<Anuncio>> obtenerAnuncioPorRefinarConMunicipio(int idMunicipio){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        Par<Exception, List<Anuncio>> res;

        try {

            String sentencia = "SELECT *\n" +
                    "FROM anuncio an\n" +
                    "WHERE an.id NOT IN (\n" +
                    "    SELECT anTipIn.anuncio_id\n" +
                    "    FROM anuncio_tipoContrato_inmueble anTipIn\n" +
                    ")\n" +
                    "AND an.municipio_id = :idMunicipio\n" +
                    "ORDER BY an.municipio_id ASC;";

            Query query = entityManager.createNativeQuery(sentencia, Anuncio.class);
            query.setParameter("idMunicipio", idMunicipio);
            res = new Par<>(null, query.getResultList());

        }catch (Exception e){
            res = new Par<>(e, null);
        }

        entityManager.close();

        return res;

    }

    public Par<Exception, List<Integer>> obtenerIdsMunicipiosAnunciosPorRefinar(){
        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        Par<Exception, List<Integer>> res;

        try {

            String sentencia = "SELECT DISTINCT an.municipio_id\n" +
                    "FROM anuncio an\n" +
                    "WHERE an.id NOT IN (\n" +
                    "    SELECT anTipIn.anuncio_id\n" +
                    "    FROM anuncio_tipoContrato_inmueble anTipIn\n" +
                    ")\n" +
                    "ORDER BY an.municipio_id ASC;";

            Query query = entityManager.createNativeQuery(sentencia);
            res = new Par<>(null, query.getResultList());

        }catch (Exception e){
            res = new Par<>(e, null);
        }

        entityManager.close();
        entityManager.close();

        return res;
    }

    public Par<Exception, List<Integer>> obtenerIdsAnunciosPorRefinar(){

        EntityManager entityManager = Utils.crearEntityManager();

        Par<Exception, List<Integer>> res;

        try {

            String sentencia = "SELECT an.id\n" +
                    "FROM anuncio an\n" +
                    "WHERE an.id NOT IN (\n" +
                    "    SELECT anTipIn.anuncio_id\n" +
                    "    FROM anuncio_tipoContrato_inmueble anTipIn\n" +
                    ")\n" +
                    "ORDER BY an.municipio_id ASC, an.id ASC;";

            Query query = entityManager.createNativeQuery(sentencia, Integer.class);
            res = new Par<>(null, query.getResultList());

        }catch (Exception e){
            res = new Par<>(e, null);
        }

        entityManager.close();

        return res;
    }

    public Par<Exception, Long> obtenerRecuentoAnunciosPorRefinar(){

        EntityManager entityManager = Utils.crearEntityManager();

        Par<Exception, Long> res;

        try {

            String sentencia = "SELECT COUNT(an.id)\n" +
                    "FROM anuncio an\n" +
                    "WHERE an.id NOT IN (\n" +
                    "    SELECT anTipIn.anuncio_id \n" +
                    "    FROM anuncio_tipoContrato_inmueble anTipIn \n" +
                    ")";

            Query query = entityManager.createNativeQuery(sentencia, Long.class);
            res = new Par<>(null, (Long) query.getSingleResult());

        } catch (Exception e){
            res = new Par<>(e, null);
        }

        entityManager.close();

        return res;

    }
    // -----------------
}
