package controlador.scrapers;

public enum TipoScraper {
    FOTOCASA(1);

    private int id;
    private TipoScraper(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
