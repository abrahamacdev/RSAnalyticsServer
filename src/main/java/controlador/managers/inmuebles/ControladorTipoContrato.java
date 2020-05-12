package controlador.managers.inmuebles;

import modelo.pojo.rest.Tipo;
import modelo.pojo.scrapers.TipoContrato;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class ControladorTipoContrato {

    // --- Read ---
    public Par<Exception, TipoContrato> buscarTipoContrato(utilidades.scrapers.TipoContrato tipoContrato){

        EntityManager entityManager = Utils.crearEntityManager();
        Par res = null;

        try {

            Query query = entityManager.createQuery("FROM TipoContrato AS tp WHERE tp.nombre = :nombre");
            query.setParameter("nombre", Utils.capitalize(tipoContrato.name()));

            TipoContrato tipoRes = (TipoContrato) query.getSingleResult();
            res= new Par(null, tipoContrato);

        }catch (Exception e){
            res = new Par(e,null);
        }

        entityManager.close();
        return res;
    }

    public Par<Exception, TipoContrato> buscarTipoContratoConId(int id, EntityManager entityManager){

        try {

            Query query = entityManager.createQuery("FROM TipoContrato AS tp WHERE tp.id = :id");
            query.setParameter("id", id);

            TipoContrato tipoRes = (TipoContrato) query.getSingleResult();
            return new Par<>(null, tipoRes);

        }catch (Exception e){
            return new Par(e,null);
        }
    }

    public Par<Exception, List<TipoContrato>> obtenerTiposContratos(){

        EntityManager entityManager = Utils.crearEntityManager();
        Par res = null;

        try {

            Query query = entityManager.createQuery("FROM TipoContrato");

            List<TipoContrato> tipoRes = (List<TipoContrato>) query.getResultList();
            res= new Par(null, tipoRes);

        }catch (Exception e){
            res = new Par(e,null);
        }

        entityManager.close();
        return res;

    }
    // ------------


}
