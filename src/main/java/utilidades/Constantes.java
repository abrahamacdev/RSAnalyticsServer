package utilidades;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategy;

public class Constantes {

    // API Rest
    public final static String RUTA_RELATIVA_MANEJADORES = "controlador/rest/manejadores";
    public final static String PAQUETE_MANEJADORES = "controlador.rest.manejadores";

    public final static String RESPUESTA_MSG_KEY = "msg";

    // Server
    public final static String NOMBRE_APP = "RSAnalytics";
    public final static boolean HAY_SUFICIENTES_HILOS = Runtime.getRuntime().availableProcessors() >= 4;
    public static int HILOS_PARA_ACEPTADOR;
    public static int HILOS_PARA_SCRAPER;
    public static int HILOS_MANEJADORES_PETICIONES;
    static {
        // Si tenemos suficientes hilos comprobaremos cuantos se dedicaran a cada cosa
        if (HAY_SUFICIENTES_HILOS){
            HILOS_PARA_ACEPTADOR = 1;
            HILOS_PARA_SCRAPER = Runtime.getRuntime().availableProcessors() == 4 ? 1 : 2;
            HILOS_MANEJADORES_PETICIONES = Runtime.getRuntime().availableProcessors() - HILOS_PARA_ACEPTADOR - HILOS_PARA_SCRAPER - 1;
        }
    }

    // Seguridad
    public final static int COSTO_CIFRADO_BCRYPT = 14;
    public final static BCrypt.Version VERSION_BCRYPT = BCrypt.Version.VERSION_2Y;
    public final static LongPasswordStrategy ESTRATEGIA_CONTRASENIAS_LARGAS = LongPasswordStrategies.truncate(VERSION_BCRYPT);

    // Propiedades
    public final static String NOMBRE_ARCHIVO_PROPIEDADES = "server.properties";
    public final static String PROP_PUERTO_HTTP = "server.http.port";
    public final static String PROP_PUERTO_HTTPS = "server.https.port";
    public final static String PROP_KEYSTORE_PATH = "server.keystore.path";
    public final static String PROP_KEYSTORE_PASS = "server.keystore.pass";
}
