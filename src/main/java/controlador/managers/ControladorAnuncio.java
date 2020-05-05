package controlador.managers;

import modelo.pojo.rest.Grupo;
import modelo.pojo.scrapers.Anuncio;
import modelo.pojo.scrapers.ClaveAtributoAnuncio;
import modelo.pojo.scrapers.atributo_anuncio.AtributoAnuncio;
import org.hibernate.Session;
import utilidades.Constantes;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ControladorAnuncio {

    // ------ Create -----
    public int guardarAnuncios(List<Anuncio> anuncios){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        // Obtenemos una instancia "fresca" de los objetos "ClaveAtributo"
        /*ControladorAtributo controladorAtributo = new ControladorAtributo();
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
        controladorAtributo.actualizarClaves(clavesSinRepetir, entityManager);*/

        entityTransaction.begin();

        int guardados = 0;
        int batch = 0;
        for (Anuncio anuncio : anuncios){

            // Cada cierto tiempo hacemos un flush
            if (batch == Constantes.TAMANIO_BATCH_HIBERNATE){
                entityManager.flush();
                entityManager.clear();
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
            System.exit(1);
            return new Par<>(e, null);
        }
    }
    // -------------------
}
