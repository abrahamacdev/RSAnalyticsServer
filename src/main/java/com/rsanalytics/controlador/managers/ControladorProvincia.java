package com.rsanalytics.controlador.managers;

import com.rsanalytics.modelo.pojo.Provincia;
import org.hibernate.Session;
import com.rsanalytics.utilidades.Constantes;
import com.rsanalytics.utilidades.Par;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ControladorProvincia {


    // ----- Update -----
    public List<Provincia> guardarOActualizar(List<Provincia> provincias, EntityManager entityManager) {

        final List<Provincia> provinciasActualizadas = new ArrayList<>(provincias.size());

        List<String> nombres = provincias.stream()
                                .map(provincia -> provincia.getNombre())
                                .collect(Collectors.toList());


        Par<Exception, List<Provincia>> provinciasEncontradas = buscarProvinciasPorNombres(nombres, entityManager);

        // No ocurrio ningun error
        if (provinciasEncontradas.getPrimero() == null){

            Session session = entityManager.unwrap(Session.class);

            provinciasActualizadas.addAll(provinciasEncontradas.getSegundo());

            List<Provincia> provinciasAGuardar = provincias.stream()
                    .filter(provincia -> !provinciasActualizadas.contains(provincia))
                    .collect(Collectors.toList());

            int batch = 0;
            for (int i=0; i<provinciasAGuardar.size(); i++){

                if (batch == Constantes.TAMANIO_BATCH_HIBERNATE){
                    entityManager.flush();
                    entityManager.clear();
                    batch = 0;
                }

                try {

                    Provincia aGuardar = provinciasAGuardar.get(i);

                    session.save(aGuardar);
                    aGuardar.getId();
                    provinciasActualizadas.add(aGuardar);

                }catch (Exception e){
                    e.printStackTrace();
                }

                batch++;
            }

        }

        return provinciasActualizadas;
    }
    // ------------------

    // ----- Read -----

    /**
     * Buscamos una provincia a partir del nombre
     * @param nombre
     * @param entityManager
     * @return
     *          null, null -> No se encontro la provincia buscada
     *          null, Provincia -> Se encontro la provincia buscada
     *          Exception, null -> Algo salio mal
     */
    public Par<Exception, Provincia> buscarProvinciasPorNombres(String nombre, EntityManager entityManager){

        try{

            Query query = entityManager.createQuery("FROM Provincia AS pro WHERE pro.nombre = :nombre", Provincia.class);
            query.setParameter("nombre", nombre);
            return new Par<>(null, (Provincia) query.getSingleResult());

        } catch (Exception e){
            return new Par<>(e, null);
        }
    }

    /**
     * Buscamos provincias a partir de los nombres pasadoos por parametros
     * @param nombres
     * @param entityManager
     * @return
     *          null, List<Provincia> -> Provincias encontradas
     *          Exception, null -> Algo salio mal
     */
    public Par<Exception, List<Provincia>> buscarProvinciasPorNombres(List<String> nombres, EntityManager entityManager){

        try{

            Query query = entityManager.createQuery("FROM Provincia AS pro WHERE pro.nombre IN :nombre", Provincia.class);
            query.setParameter("nombre", nombres);
            return new Par<>(null, query.getResultList());

        } catch (Exception e){
            e.printStackTrace();
            return new Par<>(e, null);
        }
    }
    // ----------------
}
