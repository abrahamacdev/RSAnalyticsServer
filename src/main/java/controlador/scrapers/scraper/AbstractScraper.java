package controlador.scrapers.scraper;

public abstract class AbstractScraper {

    abstract public void comenzar();

    public void pausar(){};

    public void reanudar(){};

    abstract public void detener();
}
