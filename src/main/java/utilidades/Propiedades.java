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

                properties.load(new FileInputStream("./" + Constantes.NOMBRE_ARCHIVO_PROPIEDADES));
            } catch (IOException ex) {
                Logger.error("Error cargando el archivo de propiedades", ex);
            }
        }

        return properties;
    }
}
