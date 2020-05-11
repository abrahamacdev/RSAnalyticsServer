package controlador.scrapers;

import controlador.scrapers.TipoScraper;
import modelo.pojo.scrapers.Anuncio;

import java.util.List;

public interface OnScraperListener {

    public void onScraped (List<Anuncio> anuncio, TipoScraper tipoScraper);
    public void onError (Exception e, TipoScraper tipoScraper);
    public void onTerminado(TipoScraper tipoScraper);

    public interface OnAcabadoListener {

        void onAcabadoExitoso(TipoScraper tipoScraper);

        void onAcabadoErroneo(Exception e, TipoScraper tipoScraper);
    }

}
