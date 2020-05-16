package controlador.managers.informes;

import controlador.managers.ControladorUsuario;
import controlador.managers.inmuebles.ControladorTipoContrato;
import modelo.pojo.scrapers.Informe;
import modelo.pojo.scrapers.TipoContrato;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class ControladorInforme {

    ControladorUsuario controladorUsuario = new ControladorUsuario();
    ControladorTipoContrato controladorTipoContrato = new ControladorTipoContrato();

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
