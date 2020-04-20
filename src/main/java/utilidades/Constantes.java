package utilidades;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategy;
import io.jsonwebtoken.SignatureAlgorithm;

public class Constantes {

    // API Rest
    public static class REST {

        // Recursos relacionadas con el tratamiento de usuarios
        public enum USUARIO{

            //PATH
            USUARIO_PATH("/usuario"),

            // ENDPOINTs
            LOGIN_ENDPOINT("/login"),
            REGISTRO_ENDPOINT("/registro");


            public final String value;
            private USUARIO(String v){
                this.value = v;
            }
        }

        // Recursos relacionadas con el tratamiento de grupos
        public enum GRUPO {

            GRUPO_PATH("/grupo"),

            REGISTRO_ENDPOINT("/registro"),
            DATOS_GRUPO_ENDPOINT("/datosGenerales"),
            BUSCAR_ENDPOINT("/buscar"),
            ABANDONAR_ENDPOINT("/abandonar");


            public final String value;
            private GRUPO(String v){
                this.value = v;
            }
        }

        // Claves para las respuestas de los jsons
        public enum RESPUESTAS_KEYS {

            MSG("msg"),
            GRUPO("grupo"),
            TOKEN("token");

            public final String value;
            private RESPUESTAS_KEYS(String v){
                this.value = v;
            }
        }

        // Claves de las peticiones entrantes
        public enum PETICIONES_KEYS {

            AUTHORIZATION_HEADER("Authorization");

            public final String value;
            private PETICIONES_KEYS(String v){
                this.value = v;
            }
        }

        private REST(){}
    }


    // Server
    public final static String NOMBRE_APP = "RSAnalytics";
    public final static String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public final static String NOMBRE_REGEX = "^(?:[A-Za-zÑñÁáÉéÍíÓóÚú])+(?:\\s(?:[A-Za-zÑñÁáÉéÍíÓóÚú])+)*";
    public final static int LONGITUD_MINIMA_CONTRASENIAS = 8;
    public final static String CONTRASENIA_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\\$%\\^&\\*])";
    public final static String TELEFONO_REGEX = "^[0-9]{9}";

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

    public final static String RUTA_RELATIVA_MANEJADORES = "controlador/rest/handlers";
    public final static String PAQUETE_MANEJADORES = "controlador.rest.handlers";


    // Seguridad
    public final static SignatureAlgorithm VERSION_RSA = SignatureAlgorithm.RS384;
    public final static int COSTO_CIFRADO_BCRYPT = 14;
    public final static BCrypt.Version VERSION_BCRYPT = BCrypt.Version.VERSION_2Y;
    public final static LongPasswordStrategy ESTRATEGIA_CONTRASENIAS_LARGAS = LongPasswordStrategies.truncate(VERSION_BCRYPT);

    // Propiedades
    public final static String NOMBRE_ARCHIVO_PROPIEDADES = "server.properties";
    public final static String PROP_PUERTO_HTTP = "server.http.port";
    public final static String PROP_PUERTO_HTTPS = "server.https.port";
    public final static String PROP_KEYSTORE_PATH = "server.keystore.path";
    public final static String PROP_KEYSTORE_PASS = "server.keystore.pass";

    // JWT
    public final static String JWT_KEY_CREADOR = "iss";
    public final static String JWT_KEY_FECHA_EXPIRACION = "exp";
    public final static String JWT_KEY_FECHA_CREACION = "iat";
    public final static String JWT_KEY_ID_TOKEN = "id";
    public final static String JWT_KEY_SUJETO = "sub";
    public final static String JWT_KEY_ADMIN = "admin";

    public final static int TIEMPO_EXPIRACION_TOKEN_ACCESO = 180; // Minutos

    private Constantes(){}
}
