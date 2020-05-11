package controlador.scrapers;

import controlador.managers.anuncios.ControladorAnuncio;
import controlador.scrapers.scraper.AbstractScraper;
import controlador.scrapers.scraper.fotocasa.ScraperFotocasa;
import modelo.pojo.scrapers.Anuncio;
import org.tinylog.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class ManejadorScrapers implements OnScraperListener {

    private ExecutorService piscinaScraper;

    private ControladorAnuncio controladorAnuncio;
    private OnScraperListener.OnAcabadoListener onAcabadoListener;

    public ManejadorScrapers(ExecutorService piscinaScraper){
        this.piscinaScraper = piscinaScraper;
        this.controladorAnuncio = new ControladorAnuncio();
    }

    public void scrap(){

        // Lanzamos los scrapers de forma asincrona
        for (TipoScraper tipo : TipoScraper.values()) {
            lanzarAsync(crearScraperDeTipo(tipo));
        }
    }

    public void scrap(OnScraperListener.OnAcabadoListener onAcabadoListener){
        this.onAcabadoListener = onAcabadoListener;
        scrap();
    }


    @Override
    public void onScraped(List<Anuncio> anuncios, TipoScraper tipoScraper) {

        Logger.info("Vamos a guardar " + anuncios.size() + " anuncios");

        // Guardamos los anuncios recogidos en esta tanda
        controladorAnuncio.guardarAnuncios(anuncios);
    }

    @Override
    public void onError(Exception e, TipoScraper tipoScraper) {

        Logger.error("Ocurrio un error con algun scraper");
        // Volvemos a lanzar el scraper
        lanzarAsync(crearScraperDeTipo(tipoScraper));

        if (onAcabadoListener != null){
            onAcabadoListener.onAcabadoErroneo(e, tipoScraper);
        }
    }

    @Override
    public void onTerminado(TipoScraper tipoScraper) {

        Logger.info("Hemos terminador de obtener la informacion");

        if (onAcabadoListener != null){
            onAcabadoListener.onAcabadoExitoso(tipoScraper);
        }
    }

    private AbstractScraper crearScraperDeTipo(TipoScraper tipoScraper){

        switch (tipoScraper){

            case FOTOCASA:
                return new ScraperFotocasa(this);
        }

        return null;
    }

    private void lanzarAsync(AbstractScraper abstractScraper){

        if (abstractScraper != null){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    abstractScraper.comenzar();
                }
            };

            piscinaScraper.submit(runnable);
        }
    }
}
