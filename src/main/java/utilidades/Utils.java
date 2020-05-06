package utilidades;

import com.google.common.io.Files;
import com.mysql.cj.util.TimeUtil;
import io.javalin.core.util.FileUtil;
import kotlin.text.Charsets;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.tinylog.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utilidades.Constantes.*;

public class Utils {

    private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("RSAnalytics");

    public static EntityManager crearEntityManager(){
        try {
            synchronized (entityManagerFactory){
                if (entityManagerFactory.isOpen()){
                    return entityManagerFactory.createEntityManager();
                }
            }
        }catch (Exception e){
            Logger.error("No se pudo crear una sesion para Hibernate");
        }
        return null;
    }

    public static boolean cerrarEntityManagerFactory(){
        synchronized (entityManagerFactory){
            if (entityManagerFactory.isOpen()){
                entityManagerFactory.close();
                return true;
            }
            return false;
        }
    }


    public static String nombreArchivoSinExtension(String nombreArchivo){
        String[] spliteado = nombreArchivo.split("\\.");

        if (spliteado.length > 1){
            String nombre = "";
            for (int i=0; i<spliteado.length-1; i++){
                nombre += spliteado[i];
            }


            return nombre;
        }

        return null;
    }

    public static boolean correoValido(String correo){
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(correo);

        return matcher.find();
    }

    public static boolean nombreValido(String nombre){

        Pattern pattern = Pattern.compile(NOMBRE_REGEX);
        Matcher matcher = pattern.matcher(nombre);

        return matcher.find();
    }

    public static boolean contraseniaValida(byte[] contrasenia){

        char[] tempContrasenia = new char[contrasenia.length];

        for (int i=0; i<contrasenia.length; i++){
            tempContrasenia[i] = (char) contrasenia[i];
        }

        boolean longitudNecesaria = tempContrasenia.length >= LONGITUD_MINIMA_CONTRASENIAS;

        // Si tiene la longitud necesaria...
        if (longitudNecesaria){

            Pattern pattern = Pattern.compile(CONTRASENIA_REGEX);
            Matcher matcher = pattern.matcher(CharBuffer.wrap(tempContrasenia));

            return matcher.find();
        }
        return false;
    }

    public static boolean contraseniaValida(String contrasenia){

        Pattern pattern = Pattern.compile(CONTRASENIA_REGEX);
        Matcher matcher = pattern.matcher(contrasenia);

        return matcher.find();
    }

    public static boolean telefonoValido(String telefono){

        Pattern pattern = Pattern.compile(TELEFONO_REGEX);
        Matcher matcher = pattern.matcher(telefono);

        return matcher.find();
    }

    public static void copiarAlPortapapeles(String txt){

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = new StringSelection(txt);
        clipboard.setContents(transferable, null);
    }

    public static void esperarA(Object object, long timeout, Function<Object,Boolean> function){

        if (timeout <= 0){
            return;
        }

        Function<Object, Boolean> temp = new Function<Object, Boolean>() {

            long tmOut = timeout;

            @Override
            public Boolean apply(Object object) {

                boolean res = false;
                do {

                    res = function.apply(object).booleanValue();

                    if (!res){
                        tmOut -= 50;
                        esperar(50);
                    }

                }while (tmOut> 0 && !res);

                return true;
            }
        };

        temp.apply(object);
    }

    public static void esperarA(Object object, Function<Object,Boolean> function){

        while (true){

            if (function.apply(object).booleanValue()){
                break;
            }

            else {
                esperar(25);
            }
        }
    }

    public static void esperar(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static long cronometrarConNanos(Runnable runnable){

        long start = System.nanoTime();
        runnable.run();
        return System.nanoTime() - start;
    }

    public static long nano2Sec(long nanos){
        return TimeUnit.SECONDS.convert(nanos, TimeUnit.NANOSECONDS);
    }


    public static boolean dominioCoincideCon(String url, String buscado){

        URI uri = null;
        try {
            uri = new URI(url);
            String domain = uri.getHost();

            return domain.equals(buscado);

        } catch (URISyntaxException e) {

        }
        return false;
    }

    public static String capitalize(String palabra){
        if (palabra == null || palabra.length() == 0) {
            return palabra;
        }
        return palabra.substring(0, 1).toUpperCase() + palabra.substring(1);
    }

    public static String convertirCertificadoEnergetico2Letra(double certNum){

        int temp = (int) certNum;

        switch (temp){

            case 1:
                return "A";

            case 2:
                return "B";

            case 3:
                return "C";

            case 4:
                return "D";

            case 5:
                return "E";

            case 6:
                return "F";

            case 7:
                return "G";
        }

        return "";
    }

    public static int guardarJsons2Archivo(String ruta, boolean nombreRandom, List<JSONObject> jsons){

        File dir = new File(ruta);

        int guardados = 0;

        // Ruta no valida
        if (dir.isFile()){
            return 0;
        }

        // CReamos la carpeta si no existe
        if (!dir.exists()){
            dir.mkdir();
        }

        int i=1;
        for (JSONObject json : jsons) {

            File archivo = new File(dir.getPath() + "/doc" + i + ".json");
            if (nombreRandom){
                archivo = new File(dir.getPath() + "/" + UUID.randomUUID().toString() + ".json");
            }

            try {
                FileUtils.writeStringToFile(archivo, json.toJSONString(), Charsets.UTF_8);
                guardados++;

            } catch (IOException e) {
                // No se pudo guardar el archivo
            }

            i++;
        }

        return guardados;
    }

    // TODO Eliminar
    public static void convertirArchivoExtras(){
        File extras = new File("/home/abraham/Documentos/Todos_Campos");
        try {
            List<String> lineas = Files.readLines(extras, Charsets.UTF_8);
            String paraSql = "";

            for (String linea : lineas) {
                paraSql += "(\'" + linea + "\'),";
            }

            File lineaFinal = new File("/home/abraham/Documentos/Campos_Finales");
            FileUtils.write(lineaFinal, paraSql);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
