package controlador.scrapers;

import controlador.managers.ControladorAnuncio;
import controlador.scrapers.scraper.AbstractScraper;
import controlador.scrapers.scraper.fotocasa.ScraperFotocasa;
import modelo.pojo.rest.Tipo;
import modelo.pojo.scrapers.Anuncio;
import org.tinylog.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class ManejadorScrapers implements OnScraperListener {

    private ExecutorService piscinaScraper;

    private ScraperFotocasa scraperFotocasa;
    private ControladorAnuncio controladorAnuncio;

    public ManejadorScrapers(ExecutorService piscinaScraper){
        this.piscinaScraper = piscinaScraper;
        this.scraperFotocasa = new ScraperFotocasa(this);
        this.controladorAnuncio = new ControladorAnuncio();
    }

    public void scrap(){

        // Lanzamos los scrapers de forma asincrona
        for (TipoScraper tipo : TipoScraper.values()) {
            lanzarAsync(crearScraperDeTipo(tipo));
        }
    }


    @Override
    public void onScraped(List<Anuncio> anuncios, TipoScraper tipoScraper) {

        Logger.info("Vamos a guardar " + anuncios.size() + " anuncios");
        controladorAnuncio.guardarAnuncios(anuncios);
    }

    @Override
    public void onError(Exception e, TipoScraper tipoScraper) {
        Logger.error("Ocurrio un error mientras se obtenian los datos de internet");

        // Volvemos a lanzar el scraper
        lanzarAsync(crearScraperDeTipo(tipoScraper));
    }

    @Override
    public void onTerminado(TipoScraper tipoScraper) {

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
