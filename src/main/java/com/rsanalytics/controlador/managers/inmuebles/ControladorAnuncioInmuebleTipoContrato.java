package com.rsanalytics.controlador.managers.inmuebles;

import com.rsanalytics.controlador.managers.anuncios.ControladorAnuncio;
import com.rsanalytics.modelo.pojo.scrapers.Anuncio;
import com.rsanalytics.modelo.pojo.scrapers.TipoContrato;
import com.rsanalytics.modelo.pojo.scrapers.anuncio_inmueble_tipoContrato.AnuncioInmuebleTipoContrato;
import com.rsanalytics.utilidades.Constantes;
import com.rsanalytics.utilidades.Par;

import javax.persistence.EntityManager;
import java.util.List;

public class ControladorAnuncioInmuebleTipoContrato {

    private ControladorAnuncio controladorAnuncio = new ControladorAnuncio();
    private ControladorTipoContrato controladorTipoContrato = new ControladorTipoContrato();

    // --- Create ---
    public int guardarAnunciosLigados(List<AnuncioInmuebleTipoContrato> anunciosLigados, EntityManager entityManager){

        int guardados = 0;
        int batch = 0;
        for (AnuncioInmuebleTipoContrato anuncioLigado : anunciosLigados){

            // Cada cierto tiempo hacemos un flush
            if (batch == Constantes.TAMANIO_BATCH_HIBERNATE){
                entityManager.flush();
                entityManager.clear();
                batch = 0;
            }

            // Guardamos el anuncio
            Par<Exception, AnuncioInmuebleTipoContrato> res = guardarAnuncioLigado(anuncioLigado, entityManager);
            if (res.getSegundo() != null){
                guardados++;
            }

            batch++;
        }

        return guardados;
    }

    public Par<Exception, AnuncioInmuebleTipoContrato> guardarAnuncioLigado(AnuncioInmuebleTipoContrato anuncioLigado, EntityManager entityManager){

        try {

            Par<Exception, Anuncio> resBusAnun = controladorAnuncio.obtenerAnuncioConId(anuncioLigado.getAnuncio().getId(), entityManager);
            Par<Exception, TipoContrato> resBusTipCon = controladorTipoContrato.buscarTipoContratoConId(anuncioLigado.getTipoContrato().getId(), entityManager);

            if (resBusAnun.getPrimero() != null){
                return new Par<>(resBusAnun.getPrimero(), null);
            }

            if (resBusTipCon.getPrimero() != null){
                return new Par<>(resBusTipCon.getPrimero(), null);
            }

            anuncioLigado.setAnuncio(resBusAnun.getSegundo());
            anuncioLigado.setTipoContrato(resBusTipCon.getSegundo());

            entityManager.persist(anuncioLigado);

        }catch (Exception e){
            e.printStackTrace();
            return new Par<>(e, null);
        }

        return new Par<>(null, anuncioLigado);
    }
    // --------------

}
