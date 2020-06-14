package com.rsanalytics.utilidades;

import com.rsanalytics.Main;
import org.apache.commons.io.FileUtils;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

public class PrimeraVez {

    public static void init(){

        PrimeraVez primeraVez = new PrimeraVez();

        // No tenemos disponibles los directorios necesarios para funcionar correctamente
        if (!primeraVez.realizarComprobacionesPrevias()){
            System.exit(1);
        }

        if (primeraVez.esPrimeraVez()){

            // Copiamos las plantillas y loos archivos de configuracioon al directorioo de recursos de la aplicacion
            if (!primeraVez.copiarDatosNecesarios()){
                Logger.error("No pudimos copiar los datos necesarios para el funcionamiento del sistema");
                System.exit(1);
            }
        }

        // TODO Eliminar en produccion
        //primeraVez.copiarPlantillas2DirectorioRecursos();

        // Establecemos el directorio en el que se encuentra el chromedriver
        System.setProperty("webdriver.chrome.driver", Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_CONFIGURACION + "/chromedriver");

    }

    private boolean realizarComprobacionesPrevias(){

        int necesarios = 5;
        int recuento = 0;

        recuento += existeRutaRecursos() ? 1 : 0;
        recuento += existeRutaPlantillas() ? 1 : 0;
        recuento += existeRutaInformes() ? 1 : 0;
        recuento += existeRutaTemporal() ? 1 : 0;
        recuento += existeRutaConfiguracion() ? 1 : 0;

        return recuento == necesarios;
    }

    private boolean existeRutaPlantillas(){

        File rutaPlantillas = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_PLANTILLAS);

        // EL directoorio no existee
        if (!rutaPlantillas.exists()){

            // Creamos el directorio de recursos
            rutaPlantillas.mkdir();

            // No poodemos crear el directorio
            if (!rutaPlantillas.canWrite() || !rutaPlantillas.canRead()){
                Logger.error("No podemos acceder al directorio de las plantillas");
                return false;
            }

        }

        return rutaPlantillas.canWrite() && rutaPlantillas.canRead();
    }

    private boolean existeRutaInformes(){

        File rutaInformes = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_INFORMES);

        // EL directoorio no existee
        if (!rutaInformes.exists()){

            // Creamos el directorio de recursos
            rutaInformes.mkdirs();

            // No poodemos crear el directorio
            if (!rutaInformes.canWrite() || !rutaInformes.canRead()){
                Logger.error("No podemos acceder al directorio de los informes");
                return false;
            }
        }

        return rutaInformes.canWrite() && rutaInformes.canRead();
    }

    private boolean existeRutaTemporal(){

        File rutaTemporal = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_TEMPORAL);

        // EL directoorio no existee
        if (!rutaTemporal.exists()){

            // Creamos el directorio de recursos
            rutaTemporal.mkdirs();

            // No poodemos crear el directorio
            if (!rutaTemporal.canWrite() || !rutaTemporal.canRead()){
                Logger.error("No podemos acceder al directorio temporal");
                return false;
            }
        }

        return rutaTemporal.canWrite() && rutaTemporal.canRead();
    }

    private boolean existeRutaConfiguracion(){

        File rutaConfiguracion = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_CONFIGURACION);
        rutaConfiguracion.setExecutable(true,false);

        // EL directoorio no existee
        if (!rutaConfiguracion.exists()){

            // Creamos el directorio de recursos
            rutaConfiguracion.mkdirs();

            // No poodemos crear el directorio
            if (!rutaConfiguracion.canWrite() || !rutaConfiguracion.canRead()){
                Logger.error("No podemos acceder al directorio de configuración");
                return false;
            }
        }

        return rutaConfiguracion.canWrite() && rutaConfiguracion.canRead();

    }

    private boolean existeRutaRecursos(){

        File rutaRecursos = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS);

        // EL directoorio no existee
        if (!rutaRecursos.exists()){

            // Creamos el directorio de recursos
            rutaRecursos.mkdir();

            // No poodemos crear el directorio
            if (!rutaRecursos.canWrite() || !rutaRecursos.canRead()){
                Logger.error("No podemos acceder a los recursos de la aplicacion");
                return false;
            }
        }

        return rutaRecursos.canWrite() && rutaRecursos.canRead();
    }

    private boolean esPrimeraVez(){
        File rutaPlantillas = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_PLANTILLAS);

        return rutaPlantillas.listFiles().length == 0;
    }



    private boolean copiarDatosNecesarios(){

        int necesarios = 2;
        int recuento = 0;

        recuento += copiarArchivosConf2DirectorioConf() ? 1 : 0;
        recuento += copiarPlantillas2DirectorioRecursos() ? 1 : 0;

        return recuento == necesarios;
    }

    private boolean copiarPlantillas2DirectorioRecursos(){

        File plantillas = new File(Main.class.getResource("../../plantillas").getPath());
        File directorioPlantillasApp = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_PLANTILLAS);

        try {
            FileUtils.copyDirectory(plantillas, directorioPlantillasApp);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean copiarArchivosConf2DirectorioConf() {

        File configuracion = new File(Main.class.getResource("../../conf").getPath());
        File directorioRecursos = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_CONFIGURACION);
        directorioRecursos.setReadable(true);
        directorioRecursos.setWritable(true);
        directorioRecursos.setExecutable(true,false);

        File chromeDriverSrc = new File(Main.class.getResource("../../chromeDriver/chromedriver").getPath());
        chromeDriverSrc.setExecutable(true,false);

        try {
            FileUtils.copyDirectory(configuracion, directorioRecursos);
            FileUtils.copyFileToDirectory(chromeDriverSrc, directorioRecursos);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
