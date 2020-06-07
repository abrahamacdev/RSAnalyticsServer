import controlador.Server;
import controlador.seguridad.TokensManejador;
import utilidades.PrimeraVez;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Para que la aplicacion funcione correctamente hay que iniciar una primera vez la aplicación,
 * esta automáticamente creará el directorio de trabajo bajo el directorio "/opt". Una vez ejecutada por primera
 * vez, tenemos que:
 *
 * 1. Ir al directorio "/opt/RSAnalytics/conf/" y dar permisos de ejecución a "chromedriver"
 *
 */
public class Main {

    public static void main(String[] args) {

        // Creamos los directorios que necesita para funcionar el sistema
        PrimeraVez.init();

        // Cargamos las claves del servidor
        TokensManejador.init();

        // Inicializamos el servidor
        new Server();
    }
}