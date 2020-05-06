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
import java.util.*;
import java.util.stream.Collectors;

public class ControladorAnuncio {

    private ControladorMunicipio controladorMunicipio = new ControladorMunicipio();
    private ControladorAtributo controladorAtributo = new ControladorAtributo();

    // ------ Create -----
    public int guardarAnuncios(List<Anuncio> anuncios){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        // Obtenemos una instancia "fresca" de los objetos "ClaveAtributo"
        ControladorAtributo controladorAtributo = new ControladorAtributo();
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
        controladorAtributo.actualizarClaves(clavesSinRepetir, entityManager);

        // Guardamos/Actualizamos los municipios para tener una instancia cacheada por hibernate y asi evitar
        // que nos de errores
        Set<Municipio> municipiosUnicos = new HashSet<>();
        anuncios.stream()
                .map(anuncio -> anuncio.getMunicipio())
                .filter(municipio -> !municipiosUnicos.contains(municipio))
                .forEach(municipio -> municipiosUnicos.add(municipio));
        List<Municipio> municipiosActualizados = controladorMunicipio.guardarOActualizarMunicipios(new ArrayList<>(municipiosUnicos), entityManager);
        HashMap<Par<String,String>, Municipio> munPorNombreCP = new HashMap<>();
        municipiosActualizados.stream()
                .forEach(mun -> munPorNombreCP.put(new Par<>(mun.getNombre(), mun.getCodigoPostal()), mun));

        // Asignamos a cada anuncio el objeto municipio que acabamos de actualizar, asi nos aseguramos de
        // que todos los anuncios hagan referencia al objeto municipio accesible por Hibernate
        List<Anuncio> anunciosParaActualizar = anuncios.stream()
                .filter(anuncio -> {
                    Par tempPar = new Par(anuncio.getMunicipio().getNombre(), anuncio.getMunicipio().getCodigoPostal());
                    return munPorNombreCP.containsKey(tempPar);
                })
                .map(anuncio -> {

                    Par tempPar = new Par(anuncio.getMunicipio().getNombre(), anuncio.getMunicipio().getCodigoPostal());
                    // Seteamos la provincia recien guardada/actualizada
                    anuncio.setMunicipio(munPorNombreCP.get(tempPar));
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
}