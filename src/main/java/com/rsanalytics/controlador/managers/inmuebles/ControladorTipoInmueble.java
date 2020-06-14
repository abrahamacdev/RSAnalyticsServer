package com.rsanalytics.controlador.managers.inmuebles;

import com.rsanalytics.modelo.pojo.scrapers.TipoInmueble;
import com.rsanalytics.utilidades.Par;
import com.rsanalytics.utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class ControladorTipoInmueble {

    // --- Read ---
    public Par<Exception, TipoInmueble> buscarTipoInmuebleConIds(int idTipo){

        Par<Exception, TipoInmueble> res = null;
        EntityManager entityManager = Utils.crearEntityManager();

        try {

            Query query = entityManager.createQuery("FROM TipoInmueble AS tp WHERE tp.id = :id");
            query.setParameter("id", convertirIdTipoInmueble2Id(idTipo));

            TipoInmueble tipoInmueble = (TipoInmueble) query.getSingleResult();
            res = new Par<>(null, tipoInmueble);

        }catch (Exception e){
            res = new Par<>(e, null);
        }

        entityManager.close();
        return res;
    }

    public Par<Exception, List<TipoInmueble>> obtenerTiposInmueble(){

        Par<Exception, List<TipoInmueble>> res = null;
        EntityManager entityManager = Utils.crearEntityManager();

        try {

            Query query = entityManager.createQuery("FROM TipoInmueble");

            List<TipoInmueble> tiposInmuebles = query.getResultList();
            res = new Par<>(null, tiposInmuebles);

        }catch (Exception e){
            res = new Par<>(e, null);
        }

        entityManager.close();
        return res;
    }
    // -----------





    /*public int obtenerIdTipoInmueble(int idTipo, int idSubTipo){

        int id = -1;

        switch (idTipo){

            // Viviendas
            case 1:
                switch (idSubTipo){

                    // Planta baja
                    case 1:
                        id = 10;
                    break;

                    // Planta intermedia
                    case 2:
                        id = 9;
                        break;

                    // Apartamento
                    case 3:
                        id = 4;
                        break;

                    // Atico
                    case 4:
                        id = 5;
                        break;

                    // Duplex
                    case 5:
                        id = 6;
                        break;

                    // Loft
                    case 6:
                        id = 7;
                        break;

                    // Estudio
                    case 7:
                        id = 8;
                        break;

                    // Finca rustica
                    case 20:
                        id = 3;
                        break;

                    // Chalet
                    case 21:
                        id = 1;
                        break;

                    // Casa adosada
                    case 22:
                        id = 2;
                        break;
                }

        }

        return id;
    }*/
    private int convertirIdTipoInmueble2Id(int idTipo){

        switch (idTipo){

            // Vivienda
            case 1:
                return 1;
        }

        return -1;
    }
}
