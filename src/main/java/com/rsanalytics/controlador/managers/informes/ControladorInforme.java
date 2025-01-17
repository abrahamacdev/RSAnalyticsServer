package com.rsanalytics.controlador.managers.informes;

import com.rsanalytics.controlador.managers.inmuebles.ControladorTipoContrato;
import com.rsanalytics.modelo.pojo.rest.Usuario;
import com.rsanalytics.modelo.pojo.scrapers.Informe;
import com.rsanalytics.modelo.pojo.scrapers.Inmueble;
import com.rsanalytics.modelo.pojo.scrapers.TipoContrato;
import com.rsanalytics.utilidades.Par;
import com.rsanalytics.utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class ControladorInforme {

    ControladorTipoContrato controladorTipoContrato = new ControladorTipoContrato();

    // --- Read ---
    public Par<Exception, Informe> obtenerInformeMasViejoPorRealizar(){

        EntityManager entityManager = Utils.crearEntityManager();

        Par<Exception, Informe> res = obtenerInformeMasViejoPorRealizar(entityManager);

        entityManager.close();

        return res;
    }

    /**
     * Obtenemos el informe mas viejo que aun esta pendiente de realizar
     * @param entityManager
     * @return  null, Informe -> Informe buscado
     *          null, null -> No hay ninggun informe que cumpla los criterios
     *          Exception, null -> Algo salio mal
     */
    public Par<Exception, Informe> obtenerInformeMasViejoPorRealizar(EntityManager entityManager){

        try {

            Query query = entityManager.createQuery("FROM Informe AS inf WHERE inf.rutaArchivo = null ORDER BY inf.id ASC");
            query.setMaxResults(1);
            Informe informe = (Informe) query.getSingleResult();

            return new Par<>(null, informe);

        } catch (NoResultException noRes){
            return new Par<>(null, null);

        } catch (Exception e){
            return new Par<>(e, null);
        }
    }

    public Par<Exception, List<Inmueble>> obtenerTodosLosInmueblesDelInforme(Informe informe){

        EntityManager entityManager = Utils.crearEntityManager();

        Par<Exception,List<Inmueble>> res = obtenerTodosLosInmueblesDelInforme(informe, entityManager);

        entityManager.close();

        return res;
    }

    public Par<Exception, List<Inmueble>> obtenerTodosLosInmueblesDelInforme(Informe informe, EntityManager entityManager){

        try {

            Query query = entityManager.createNativeQuery("SELECT inmInf.inmueble_id\n" +
                    "FROM inmueble_informe inmInf\n" +
                    "WHERE inmInf.informe_id = :id");
            query.setParameter("id", informe.getId());
            List<Integer> idsInmueblesInforme = query.getResultList();

            query = entityManager.createQuery("FROM Inmueble AS inm WHERE inm.id IN :ids");
            query.setParameter("ids", idsInmueblesInforme);

            return new Par<>(null, query.getResultList());

        }catch (Exception e){
            return new Par<>(e, null);
        }

    }

    public Par<Exception, List<Informe>> obtenerInformesDelUsuario(Usuario usuario){

        EntityManager entityManager = Utils.crearEntityManager();

        Par<Exception, List<Informe>> res = obtenerInformesDelUsuario(usuario, entityManager);

        entityManager.close();

        return res;
    }

    public Par<Exception, List<Informe>> obtenerInformesDelUsuario(Usuario usuario, EntityManager entityManager){

        try {

            Query query = entityManager.createQuery("FROM Informe AS inf WHERE inf.usuario.id = :idUsuario ORDER BY inf.fechaCreacionSolicitud DESC");
            query.setParameter("idUsuario", usuario.getId());

            return new Par<>(null, (List<Informe>) query.getResultList());

        }catch (Exception e){
            return new Par<>(e, null);
        }
    }

    public Par<Exception, Informe> obtenerInformeConId(int idInforme){

        EntityManager entityManager = Utils.crearEntityManager();

        Par<Exception, Informe> res = obtenerInformeConId(idInforme, entityManager);

        entityManager.close();

        return res;
    }

    public Par<Exception, Informe> obtenerInformeConId(int idInforme, EntityManager entityManager){

        try {

            Query query = entityManager.createQuery("FROM Informe AS inf WHERE inf.id = :idInforme");
            query.setParameter("idInforme", idInforme);
            query.setMaxResults(1);

            return new Par(null, query.getSingleResult());

        }catch (Exception e){
            return new Par<>(e, null);
        }

    }
    // ------------

    // --- Update ---
    public int actualizarInforme(Informe informe){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        int res = actualizarInforme(informe, entityManager);

        if (res == 0){
            entityTransaction.rollback();
        }

        else {
            entityTransaction.commit();
        }

        entityManager.close();

        return res;
    }

    public int actualizarInforme(Informe informe, EntityManager entityManager){

        try {

            entityManager.merge(informe);
            return 1;

        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }
    // --------------

    // --- Create ---
    public Par<Exception, Informe> guardarInforme(Informe informe){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        Par<Exception,Informe> res = guardarInforme(informe,entityManager);
        if (res.getPrimero() != null){
            transaction.rollback();
        }
        else {
            transaction.commit();
        }

        entityManager.close();
        return res;
    }

    public Par<Exception, Informe> guardarInforme(Informe informe, EntityManager entityManager){

        try {

            Par<Exception, TipoContrato> resBusTipoCon = controladorTipoContrato.buscarTipoContratoConId(informe.getTipoContrato().getId());
            if (resBusTipoCon.getPrimero() != null){
                return new Par<>(resBusTipoCon.getPrimero(), null);
            }

            informe.setTipoContrato(resBusTipoCon.getSegundo());

            entityManager.persist(informe);
            informe.getId();

            return new Par<>(null,informe);

        }catch (Exception e){
            return new Par<>(e,null);
        }
    }
    // ------------
}
