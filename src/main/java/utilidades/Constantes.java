package utilidades;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategy;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Constantes {

    // API Rest
    public static class REST {

        // Recursos relacionadas con el tratamiento de usuarios
        public enum USUARIO{

            //PATH
            USUARIO_PATH("/usuario"),
            NOTIFICACIONES_PATH("/notificaciones"),

            // ENDPOINTs
            LOGIN_ENDPOINT("/login"),
            REGISTRO_ENDPOINT("/registro"),
            NOTIFICACIONES_PENDIENTES_ENDPOINT("/hayPendientes"),
            MARCAR_NOTIFICACIONES_LEIDAS("/marcarNotificaciones");


            public final String value;
            private USUARIO(String v){
                this.value = v;
            }
        }

        // Recursos relacionadas con el tratamiento de grupos
        public enum GRUPO {

            GRUPO_PATH("/grupo"),
            INVITACION_PATH("/invitacion"),

            REGISTRO_ENDPOINT("/registro"),
            DATOS_GRUPO_ENDPOINT("/datosGenerales"),
            BUSCAR_ENDPOINT("/buscar"),
            ABANDONAR_ENDPOINT("/abandonar"),
            INVITAR_ENDPOINT("/invitar"),
            ACEPTAR_INVITACION_ENDPOINT("/aceptar"),
            RECHAZAR_INVITACION_ENDPOINT("/rechazar");


            public final String value;
            private GRUPO(String v){
                this.value = v;
            }
        }

        // Recursos relacionados con los informes
        public enum INFORME {

            INFORME_PATH("/informe"),

            SOLICITAR_ENDPOINT("/solicitar"),
            DESCARGAR_ENDPOINT("/descargar"),
            LISTAR_ENDPOINT("/listar");

            public final String value;
            private INFORME(String v){
                this.value = v;
            }
        }

        // Recursos relacionados con los municipios
        public enum MUNICIPIO {

            MUNICIPIOS_PATH("/municipios"),

            BUSCAR_PARECIDO_ENDPOINT("/buscarParecido");

            public final String value;
            private MUNICIPIO(String v){
                this.value = v;
            }
        }

        // Claves para las respuestas de los jsons
        public enum RESPUESTAS_KEYS {

            MSG("msg"),
            GRUPO("grupo"),
            TOKEN("token"),
            NOTIFICACIONES("notificaciones"),
            HAY_NOTIFICACIONES_PENDIENTES("existenNotificacionesPendientes");

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
    public final static String RUTA_DIRECTORIO_RSANALYTICS = "/opt/RSAnalytics";
    public final static String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public final static String NOMBRE_REGEX = "^(?:[A-Za-zÑñÁáÉéÍíÓóÚú])+(?:\\s(?:[A-Za-zÑñÁáÉéÍíÓóÚú])+)*";
    public final static int LONGITUD_MINIMA_CONTRASENIAS = 8;
    public final static String CONTRASENIA_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\\$%\\^&\\*])"; // Minuscula-Mayuscula-Numero-CaracterEspecial
    public final static String TELEFONO_REGEX = "^[0-9]{9}";

    public final static boolean HAY_SUFICIENTES_HILOS = Runtime.getRuntime().availableProcessors() >= 6;
    public static int HILOS_PARA_ACEPTADOR;
    public static int HILOS_PARA_SCRAPER;
    public static int HILOS_PARA_REFINADOR;
    public static int HILOS_PARA_GENERADOR_INFORMES;
    public static int HILOS_MANEJADORES_PETICIONES;
    static {
        // Si tenemos suficientes hilos comprobaremos cuantos se dedicaran a cada cosa
        if (HAY_SUFICIENTES_HILOS){
            HILOS_PARA_ACEPTADOR = 1;
            //HILOS_PARA_SCRAPER = Runtime.getRuntime().availableProcessors() == 4 ? 1 : 2;
            HILOS_PARA_SCRAPER = 1; // TODO Eliminar y descomentar la linea anterior en produccion
            HILOS_PARA_REFINADOR = 1;
            HILOS_PARA_GENERADOR_INFORMES = 1;
            //HILOS_MANEJADORES_PETICIONES = Runtime.getRuntime().availableProcessors() - HILOS_PARA_ACEPTADOR - HILOS_PARA_SCRAPER - 1;
            HILOS_MANEJADORES_PETICIONES = 1; // TODO Eliminar y descomentar la linea anterior en produccion
        }
    }

    public final static String RUTA_RELATIVA_MANEJADORES = "controlador/rest/handlers";
    public final static String PAQUETE_MANEJADORES = "controlador.rest.handlers";
    public final static String RUTA_DIRECTORIO_PLANTILLAS = "/plantillas";
    public final static String RUTA_DIRECTORIO_INFORMES = "/informes/usuarios";
    public final static String RUTA_DIRECTORIO_INFORMES_GENERADOS = "/generados";
    public final static String RUTA_DIRECTORIO_TEMPORAL = "/temp";
    public final static String RUTA_DIRECTORIO_CONFIGURACION = "/conf";
    public final static String RUTA_DIRECTORIO_INFORMES_RAW = "/raw";
    public final static int TAMANIO_BATCH_HIBERNATE = 100;

    // JWT
    public final static String JWT_KEY_CREADOR = "iss";
    public final static String JWT_KEY_FECHA_EXPIRACION = "exp";
    public final static String JWT_KEY_FECHA_CREACION = "iat";
    public final static String JWT_KEY_ID_TOKEN = "id";
    public final static String JWT_KEY_SUJETO = "sub";
    public final static String JWT_KEY_ADMIN = "admin";

    public final static int TIEMPO_EXPIRACION_TOKEN_ACCESO = 180; // Minutos

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

    // F1
    public final static HashMap<String, Double> PESOS_F1 = new HashMap(){{
        put("Numero Imagenes", 0.04);
        put("Precio", 0.13);
        put("Orientacion", 0.10);
        put("Certificados Energeticos", 0.04);
        put("Antiguedad", 0.08);
        put("Tipo Inmueble", 0.13);
        put("Bano", 0.13);
        put("Habitaciones", 0.13);
        put("M2", 0.13);
        put("Corte Coordenadas", 100.0); // 1 = 'x'mts, 0 = 0mts
        put("Coordenadas", 0.00);
        put("Extras", 0.09);
    }};

    // Generador informes
    public final static Par<Long, TimeUnit> ESPERA_SI_NO_HAY_INFORMES = new Par(5, TimeUnit.MINUTES);

    // Refinador
    public final static Par<Long, TimeUnit> ESPERA_ENTRE_REFINAMIENTOS = new Par(2, TimeUnit.HOURS);

    // Scrapers
    public final static boolean MODO_PRUEBA = true;
    public final static String RUTA_JSONS_MODO_PRUEBA = "/home/abraham/Documentos/Jsons";

    public final static long DESCANSO_ENTRE_ANUNCIOS = 1; // En segundos
    public final static Par<Integer, Integer> DESCANSO_ENTRE_PAGINAS = new Par(2,3); // En segundos
    // Un ciclo comprende la recoleccion de todos los inmuebles (independientemente de su tipo de contrato) de la pagina
    public final static Par<Integer, Integer> DESCANSO_ENTRE_CICLOS = new Par(1,3); // En horas


    private Constantes(){}
}
