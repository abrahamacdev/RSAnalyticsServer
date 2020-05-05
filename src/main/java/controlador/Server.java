package controlador;

import controlador.rest.ManejadoresManager;
import controlador.scrapers.ManejadorScrapers;
import io.javalin.Javalin;
import org.tinylog.Logger;
import utilidades.Constantes;
import utilidades.Propiedades;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final String TAG = Server.class.getName();

    private Javalin app;

    private ManejadoresManager manejadoresManager;
    private ManejadorScrapers manejadorScrapers;

    private ExecutorService piscinaHilosManejadores;
    private ExecutorService piscinaHilosScraper;
    private ExecutorService piscinaHilosConversor;
    private ExecutorService piscinaHilosGeneradorInformes;

    public Server(){
        init();
    }

    private void init(){

        // No tenemos suficientes hilos sobre los que ejecutar las tareas
        if (!Constantes.HAY_SUFICIENTES_HILOS){
            Logger.error(new RuntimeException(), "No hay suficientes hilos para ejecutar el servidor");
        }

        // Creamos las piscinas de hilos con la cantidad de hilos para cada funcion
        piscinaHilosManejadores = Executors.newFixedThreadPool(Constantes.HILOS_MANEJADORES_PETICIONES);
        piscinaHilosScraper = Executors.newFixedThreadPool(Constantes.HILOS_PARA_SCRAPER);
        piscinaHilosConversor = Executors.newFixedThreadPool(Constantes.HILOS_PARA_CONVERSOR);
        piscinaHilosGeneradorInformes = Executors.newFixedThreadPool(Constantes.HILOS_PARA_GENERADOR_INFORMES);

        lanzarScrapers();
        //crearServidor();
    }

    private void lanzarScrapers(){

        manejadorScrapers = new ManejadorScrapers(piscinaHilosScraper);
        manejadorScrapers.scrap();
    }

    private void crearServidor(){

        int puertoHTTP = Integer.valueOf(Propiedades.getProperties().getProperty(Constantes.PROP_PUERTO_HTTP));

        app = Javalin.create(config -> {
            config.enableCorsForAllOrigins();
        });

        // Dejamos que los managers subscriban las rutas que atenderan
        manejadoresManager = new ManejadoresManager(app, piscinaHilosManejadores);

        // Iniciamos el servidor
        app.start(puertoHTTP);
    }
}
