package com.rsanalytics.controlador.managers.inmuebles;

import com.rsanalytics.modelo.pojo.scrapers.ClaveAtributoInmueble;
import com.rsanalytics.utilidades.Par;
import com.rsanalytics.utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;

public class ControladorAtributoInmueble {

    // ----- Read -----
    /**
     * Obtenemos todas las posibles claves de los atributos
     * @return  null, List<ClaveAtributoInmueble> -> Listas de claves
     *          Exception, null -> Ocurrio un error desconocido
     */
    public Par<Exception, List<ClaveAtributoInmueble>> obtenerClavesPosibles(){

        EntityManager entityManager = Utils.crearEntityManager();
        Par<Exception, List<ClaveAtributoInmueble>> res;

        try {

            Query query = entityManager.createQuery("FROM ClaveAtributoInmueble");
            List<ClaveAtributoInmueble> claves = query.getResultList();

            res = new Par<>(null, claves);

        }catch (Exception e){
            res = new Par<>(e, null);
        }

        entityManager.close();

        return res;
    }

    public Par<Exception, ClaveAtributoInmueble> obtenerClaveConNombre(String nombre){

        EntityManager entityManager = Utils.crearEntityManager();
        Par<Exception, ClaveAtributoInmueble> res = null;

        try {

            Query query = entityManager.createQuery("FROM ClaveAtributoInmueble AS cla WHERE cla.nombre = :nombre");
            query.setParameter("nombre", nombre);

            res = new Par<>(null, (ClaveAtributoInmueble) query.getSingleResult());

        } catch (Exception e){
            res =  new Par<>(e, null);
        }

        entityManager.close();

        return res;
    }

    public Par<Exception, List<ClaveAtributoInmueble>> obtenerClavesConNombres(List<String> nombres, EntityManager entityManager){

        Par<Exception, List<ClaveAtributoInmueble>> res = null;

        try {

            Query query = entityManager.createQuery("FROM ClaveAtributoInmueble AS cla WHERE cla.nombre IN :nombres");
            query.setParameter("nombres", nombres);

            res = new Par(null, (List<ClaveAtributoInmueble>) query.getResultList());

        } catch (Exception e){
            res =  new Par<>(e, null);
        }

        return res;
    }

    public Par<Exception, List<Integer>> obtenerInmueblesConAtributo(HashMap<Integer, Object> valores){

        EntityManager entityManager = Utils.crearEntityManager();

        Par<Exception, List<Integer>> res = obtenerInmueblesConAtributo(valores, entityManager);

        entityManager.close();

        return res;
    }

    public Par<Exception, List<Integer>> obtenerInmueblesConAtributo(HashMap<Integer, Object> valores, EntityManager entityManager){

        try {

            String sentencia = "SELECT DISTINCT atIn.inmueble_id\n" +
                    "FROM atributoInmueble atIn\n";

            boolean primero = true;
            int i=1;
            for (Integer key : valores.keySet()){

                if (primero){
                    sentencia += "WHERE ";
                    primero = false;
                }

                else {
                    sentencia += " AND ";
                }

                sentencia += "(atIn.claveAtributoInmueble_id = :idClaAtIn" + i;

                Object valor = valores.get(key);

                if (valor instanceof String){
                    sentencia += " AND atIn.valor_cadena = :valor" + i + ")";
                }
                else {
                    sentencia += " AND atIn.valor_numerico = :valor" + i + ")";
                }

                i++;
            }

            Query query = entityManager.createNativeQuery(sentencia);

            i = 1;
            for (Integer key : valores.keySet()){
                query.setParameter("idClaAtIn" + i, key);
                query.setParameter("valor" + i, valores.get(key));
                i++;
            }

            return new Par(null, (List<Integer>) query.getResultList());

        }catch (Exception e){
            return new Par<>(e, null);
        }
    }
    // ----------------

}
