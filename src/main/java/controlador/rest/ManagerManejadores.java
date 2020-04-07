package controlador.rest;

import io.javalin.Javalin;
import org.tinylog.Logger;
import utilidades.Constantes;
import utilidades.Utils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class ManagerManejadores {

    private ArrayList<Object> manejadores = new ArrayList<>();

    public ManagerManejadores(Javalin app, ExecutorService piscina){
        init(app, piscina);
    }

    private void init(Javalin app, ExecutorService piscina){

        // Instanciamos los manejadores
        String[] nombreClasesManejadores = obtenerNombresClasesManejadores();
        instanciarManejadores(nombreClasesManejadores, app, piscina);
    }

    /**
     * Obtenemos los nombres de aquellas clases que es encuentren en el directorio "controlador/rest/manejadores"
     * @return
     */
    private String[] obtenerNombresClasesManejadores(){
        // Obtenemos los nombres de las clases que se encargaran de manejar las peticiones a la API
        String rutaManejadores = this.getClass().getClassLoader().getResource(Constantes.RUTA_RELATIVA_MANEJADORES).getFile();
        File file = new File(rutaManejadores);
        ArrayList<String> nombresClasesManejadores = new ArrayList<>();
        for (File f : file.listFiles()){
            nombresClasesManejadores.add(f.getName());
        }
        return nombresClasesManejadores.toArray(new String[nombresClasesManejadores.size()]);
    }

    /**
     * Instanciaremos los manejadores que cumplan con unos criterios en concreto
     * @param nombreClasesManejadores
     * @param app
     * @return Si se ha podido instanciar los manejadores o no
     */
    private boolean instanciarManejadores(String[] nombreClasesManejadores, Javalin app, ExecutorService piscina){

        Class[] manejadores = new Class[nombreClasesManejadores.length];
        try {

            // Creamos un objeto "Class" para cada uno de los manejadores
            for (int i=0; i<nombreClasesManejadores.length; i++){
                String temp = Utils.nombreArchivoSinExtension(nombreClasesManejadores[i]);
                manejadores[i] = Class.forName(Constantes.PAQUETE_MANEJADORES + "." + temp);
            }

            // Crearemos una instancia de aquellos manejadores que sean validos
            for (Class manejador : manejadores){
                Constructor constructorDelManejador = comprobarManejadorValido(manejador);

                if (constructorDelManejador != null){
                    constructorDelManejador.setAccessible(true);
                    Object instanciaDelManejador = constructorDelManejador.newInstance(app,piscina);
                    this.manejadores.add(instanciaDelManejador);
                }
            }

        } catch (Exception e) {
            Logger.error("Ocurrio un error al cargar los manejadores");
            return false;
        }
        return true;
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
