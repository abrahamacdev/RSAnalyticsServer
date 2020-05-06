package controlador.managers;

import modelo.pojo.Municipio;
import modelo.pojo.Provincia;
import org.hibernate.Session;
import utilidades.Constantes;
import utilidades.Par;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;

public class ControladorMunicipio {

    private ControladorProvincia controladorProvincia = new ControladorProvincia();

    public List<Municipio> guardarOActualizarMunicipios(List<Municipio> municipios, EntityManager entityManager) {

        // Guardamos/Actualizamos las provincias para tener una instancia accesible por Hibernate
        Set<Provincia> provinciasUnicas = new HashSet<>();
        municipios.stream()
                .filter(municipio -> {
                    return !provinciasUnicas.contains(municipio.getProvincia());
                })
                .forEach(municipio -> provinciasUnicas.add(municipio.getProvincia()));

        List<Provincia> provinciasActualizadas = controladorProvincia.guardarOActualizar(new ArrayList<>(provinciasUnicas), entityManager);
        HashMap<String, Provincia> provPorNombre = new HashMap<>();
                provinciasActualizadas.stream()
                                .forEach(provincia -> {
                                    provPorNombre.put(provincia.getNombre(), provincia);
                                });

        // Asignamos a cada municipio el objeto provincia que acabamos de actualizar, asi nos aseguramos de
        // que todos los municipios hagan referencia al objeto provincia accesible por Hibernate
        List<Municipio> municipiosParaActualizar = municipios.stream()
                                                    //.filter(municipio -> provPorNombre.containsKey(municipio.getProvincia().getNombre()))
                                                    .map(municipio -> {
                                                        // Seteamos la provincia recien guardada/actualizada
                                                        municipio.setProvincia(provPorNombre.get(municipio.getProvincia().getNombre()));

                                                        return municipio;
                                                    })
                                                    .collect(Collectors.toList());
        List<Municipio> municipiosActualizados = new ArrayList<>(municipiosParaActualizar.size());


        Session session = entityManager.unwrap(Session.class);

        int batch = 0;
        for (Municipio municipio : municipiosParaActualizar) {

            // Cada cierto tiempo hacemos un flush
            if (batch == Constantes.TAMANIO_BATCH_HIBERNATE){
                entityManager.flush();
                entityManager.clear();
                batch = 0;
            }

            Par<Exception, Municipio> resBusMunicipio = buscarMunicipioPorNombreYCP(municipio.getNombre(), municipio.getCodigoPostal(), entityManager);

            // No hubo error
            if (resBusMunicipio.getPrimero() == null){

                Municipio municipioObjetivo = resBusMunicipio.getSegundo();

                // NO se encontro el municipio buscado
                if (resBusMunicipio.getSegundo() == null){

                    municipioObjetivo = municipio;

                    try {
                        session.save(municipioObjetivo);
                        municipioObjetivo.getId();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Añadimos el municipio  a la lista de guardados
                municipiosActualizados.add(municipioObjetivo);
            }

            batch++;
        }

        return municipiosActualizados;
    }

    // ----- Read -----
    /**
     * Buscamos un municipio a partir del nombre y el codigo postal
     * @param nombre
     * @param codigoPostal
     * @param entityManager
     * @return
     *          null, null, -> No se enccontro el municipio
     *          null, Municipio -> Se encontro el municipio buscado
     *          Exception, null -> Algo salio mal
     */
    public Par<Exception, Municipio> buscarMunicipioPorNombreYCP(String nombre, String codigoPostal, EntityManager entityManager){

        try{

            Query query = entityManager.createQuery("FROM Municipio AS mun WHERE mun.nombre = :nombre AND mun.codigoPostal = :cp",
                    Municipio.class);

            query.setParameter("nombre", nombre);
            query.setParameter("cp", codigoPostal);

            return new Par<>(null, (Municipio) query.getSingleResult());

        } catch (NoResultException noResult){
            return new Par<>(null, null);
        }
        catch (Exception e){
            e.printStackTrace();
            return new Par<>(e, null);
        }
    }
    // ----------------

}