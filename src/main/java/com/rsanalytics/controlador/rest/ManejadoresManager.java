package com.rsanalytics.controlador.rest;

import com.rsanalytics.controlador.rest.handlers.*;
import io.javalin.Javalin;
import org.tinylog.Logger;
import com.rsanalytics.utilidades.Constantes;
import com.rsanalytics.utilidades.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


public class ManejadoresManager {

    private ArrayList<Object> manejadores = new ArrayList<>();

    public ManejadoresManager(Javalin app, ExecutorService piscina){
        init(app, piscina);
    }

    private void init(Javalin app, ExecutorService piscina){

        // Instanciamos los managers
        //String[] nombreClasesManejadores = obtenerNombresClasesManejadores();
        //instanciarManejadores(nombreClasesManejadores, app, piscina);
        instanciarManejadores(app,piscina);
    }

    private void instanciarManejadores(Javalin app, ExecutorService piscina){

        manejadores.add(new Grupos(app,piscina));
        manejadores.add(new Informes(app,piscina));
        manejadores.add(new Municipios(app,piscina));
        manejadores.add(new Notificaciones(app,piscina));
        manejadores.add(new Usuarios(app,piscina));
    }

    /**
     * Instanciaremos los managers que cumplan con unos criterios en concreto
     * @param nombreClasesManejadores
     * @param app
     * @return Si se ha podido instanciar los managers o no
     */
    private boolean instanciarManejadores(String[] nombreClasesManejadores, Javalin app, ExecutorService piscina){

        Class[] manejadores = new Class[nombreClasesManejadores.length];
        try {

            // Creamos un objeto "Class" para cada uno de los managers
            for (int i=0; i<nombreClasesManejadores.length; i++){
                String temp = Utils.nombreArchivoSinExtension(nombreClasesManejadores[i]);
                manejadores[i] = Class.forName(Constantes.PAQUETE_MANEJADORES + "." + temp);
            }

            // Crearemos una instancia de aquellos managers que sean validos
            for (Class manejador : manejadores){
                Constructor constructorDelManejador = comprobarManejadorValido(manejador);

                if (constructorDelManejador != null){
                    constructorDelManejador.setAccessible(true);

                    boolean esAbstracta = Modifier.isAbstract(manejador.getModifiers());

                    // Comprobamos que no sea una clase abstracta
                    if (!esAbstracta){
                        Object instanciaDelManejador = constructorDelManejador.newInstance(app,piscina);
                        this.manejadores.add(instanciaDelManejador);
                    }
                }
            }

        } catch (Exception e) {
            Logger.error(e,"Ocurrio un error al cargar los managers");
            return false;
        }
        return true;
    }

    /**
     * Obtenemos los nombres de aquellas clases que es encuentren en el directorio "controlador/managers/rest/managers"
     * @return
     */
    private String[] obtenerNombresClasesManejadores(){
        // Obtenemos los nombres de las clases que se encargaran de manejar las peticiones a la API
        /*String rutaManejadores = this.getClass().getClassLoader().getResource(Constantes.RUTA_RELATIVA_MANEJADORES).getFile();

        System.out.println(" ***********************************");
        File dirApp = new File(ManejadoresManager.class.getProtectionDomain().getCodeSource().getLocation().getPath());

        // Se esta ejecutando desde un jar
        if (dirApp.getName().endsWith(".jar")){
            ArrayList<Class> posiblesClases = obtenerClasesDelJar(dirApp);
            System.out.println(posiblesClases);
        }

        // Se ha ejecutado de forma normal
        else {

        }


        System.exit(1);


        File file = new File(rutaManejadores);
        ArrayList<String> nombresClasesManejadores = new ArrayList<>();
        for (File f : file.listFiles()){
            nombresClasesManejadores.add(f.getName());
        }
        return nombresClasesManejadores.toArray(new String[nombresClasesManejadores.size()]);*/
        return null;
    }

    /**
     * Para pasar el filtro hay que:
     *  1- Tener un constructor que reciba dos argumentos
     *  2- Esos argumentos tienen que ser: "Javalin, ExecutorService"
     * @param posibleManejador
     * @return
     */
    private Constructor comprobarManejadorValido(Class posibleManejador){

        Constructor[] constructores = posibleManejador.getDeclaredConstructors();
        for (Constructor constructor : constructores){
            boolean numParamsCorrecto = false;
            boolean tiposCorrectos = false;

            // El constructor tienne que tener dos parametros
            if (constructor.getParameterCount() == 2){
                numParamsCorrecto = true;
            }

            // Comprobamos el tipo de los dos argumentos
            Class[] tiposParametros = constructor.getParameterTypes();
            if (tiposParametros[0] == Javalin.class && tiposParametros[1] == ExecutorService.class){
                tiposCorrectos = true;
            }

            // Si cumple los dos requisitos, lo dejaremos pasar
            if (numParamsCorrecto && tiposCorrectos){
                return constructor;
            }
        }
        return null;
    }
}
