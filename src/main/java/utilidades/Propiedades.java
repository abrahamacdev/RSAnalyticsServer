package utilidades;

import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;


public class Propiedades {

    private static Properties properties;

    private Propiedades(){}

    public static Properties getProperties(){

        if (properties == null){
            properties = new Properties();
            try {

                // Leemos el archivo de propiedades de la ruta especificada en la variable pasada como parametro
                properties.load(new FileInputStream(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_CONFIGURACION + "/" + Constantes.NOMBRE_ARCHIVO_PROPIEDADES));
            } catch (IOException ex) {
                Logger.error("Error cargando el archivo de propiedades", ex);
            }
        }

        return properties;
    }
}
