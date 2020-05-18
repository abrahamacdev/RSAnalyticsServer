import controlador.Server;
import controlador.seguridad.TokensManejador;
import utilidades.PrimeraVez;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Para que la aplicacion funcione correctamente hay que:
 *
 * 1. Crear el directorio {@link utilidades.Constantes#RUTA_DIRECTORIO_RSANALYTICS}
 *
 * 2. Incluir los archivos "server.properties" y "keystore.jks" que se encuentran en la raiz del proyecto dentro del
 *    directorio recién creado
 *
 * 3. Copiar el "chromedriver" que se encuentra en la carpeta "chromeDriver" en el directorio "resources" de este proyecto
 *    a "/opt/chromedriver"
 *
 * 4. Añadir una variable de entorno a la VM tal que '-Dwebdriver.chrome.driver="/opt/chromedriver"'
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