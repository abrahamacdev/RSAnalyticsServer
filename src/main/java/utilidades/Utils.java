package utilidades;

import com.google.common.io.Files;
import com.mysql.cj.util.TimeUtil;
import io.javalin.core.util.FileUtil;
import kotlin.text.Charsets;
import kotlin.text.Regex;
import modelo.pojo.rest.Tipo;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.tinylog.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public static void esperar(long num, TimeUnit timeUnit){

        switch (timeUnit){

            case MILLISECONDS:
                esperar(num);
                break;

            case SECONDS:
                esperar(num * 1000);
                break;

            case MINUTES:
                esperar(num * 1000 * 60);
                break;

            case HOURS:
                esperar(num * 1000 * 60 * 60);
                break;

            case DAYS:
                esperar(num * 1000 * 60 * 60 * 24);
                break;
        }
    }

    public static <T,E> T obtenerDelMap(Map<E, Object> map, E clave, Class<T> tipo){

        if (map.containsKey(clave)){
            try {
                return tipo.cast(map.get(clave));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public static long cronometrarConNanos(Runnable runnable){

        long start = System.nanoTime();
        runnable.run();
        return System.nanoTime() - start;
    }

    public static long nano2Sec(long nanos){
        return TimeUnit.SECONDS.convert(nanos, TimeUnit.NANOSECONDS);
    }

    public static double distancia(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return (earthRadius * c);
    }

    public static double normalizarInv(double value, double min, double max) {
        return 1 - ((value - min) / (max - min));
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

    public static String mes2Texto(int num){

        switch (num){

            case 1:
                return "Enero";

            case 2:
                return "Febrero";

            case 3:
                return "Marzo";

            case 4:
                return "Abril";

            case 5:
                return "Mayo";

            case 6:
                return "Junio";

            case 7:
                return "Julio";

            case 8:
                return "Agosto";

            case 9:
                return "Septiembre";

            case 10:
                return "Octubre";

            case 11:
                return "Noviembre";

            case 12:
                return "Diciembre";
        }

        return "";
    }

    private static List<Class> obtenerSuperclasesDe(Class clase, List<Class> acumuladas){

        Class superClase = clase.getSuperclass();

        if (superClase.getCanonicalName().equals(Object.class.getCanonicalName())){
            return acumuladas;
        }

        else {
            acumuladas.add(superClase);
            return obtenerSuperclasesDe(superClase, acumuladas);
        }
    }

    public static List<Class> obtenerSuperclasesDe(Class clase){
        return obtenerSuperclasesDe(clase, new ArrayList<>());
    }

    public static Par<Integer, Integer> obtenerAniosAntiguedadInmueble (int idAntiguedad){

        switch (idAntiguedad){

            // Nuevo
            case 1:
                return new Par<>(0,0);

            // 1-5 Años
            case 2:
                return new Par<>(1,5);

            // 5-10 años
            case 3:
                return new Par<>(5,10);

            // 10-20 años
            case 4:
                return new Par<>(10,20);

            // 20-30 años
            case 5:
                return new Par<>(20,30);

            // 30-50 años
            case 6:
                return new Par<>(30,50);

            // 50-70 años
            case 7:
                return new Par<>(50,70);

            // 70-100 años
            case 8:
                return new Par<>(70,100);

            // 100 años
            case 9:
                return new Par<>(100,Integer.MAX_VALUE);
        }

        return null;
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

    // TODO Eliminar
    public static void descargarJsonDeUrlsAnuncioDetalle(String rutaArchivo){

        File archivo = new File(rutaArchivo);
        HashMap<Integer, String> datos = new HashMap<>();

        if (archivo.exists() && !archivo.isDirectory()){

            Pattern patronId = Pattern.compile("\\/([0-9]+)\\/");
            Pattern patronContrato = Pattern.compile("es\\/es\\/([A-Za-z]+)");

            BufferedReader bufferedReader = null;
            try {

                bufferedReader = new BufferedReader(new FileReader(archivo));
                String linea = bufferedReader.readLine();

                while (linea != null){

                    if (linea.startsWith("http")){

                        Matcher matcherId = patronId.matcher(linea);
                        Matcher matcherContrato = patronContrato.matcher(linea);

                        if (matcherId.find() && matcherContrato.find()){
                            int id = Integer.parseInt(matcherId.group().split("\\/")[1]);
                            String[] contratoSplited = matcherContrato.group().split("\\/");
                            String contrato = contratoSplited[contratoSplited.length - 1];
                            datos.put(id, contrato);
                        }
                    }

                    linea = bufferedReader.readLine();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Function<String,Integer> convertirTipoContrato2Int = new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {

                if (s.equals("comprar")){
                    return 1;
                }

                else if (s.equals("alquilar")){
                    return 3;
                }

                return null;
            }
        };

        Function<Par<Integer, Integer>, JSONObject> obtenerDatosDetalleAnuncio = new Function<Par<Integer, Integer>, JSONObject>() {
            @Override
            public JSONObject apply(Par<Integer, Integer> integerIntegerPar) {

                int idAnuncio = integerIntegerPar.getPrimero();
                int idContrato = integerIntegerPar.getSegundo();

                String urlDetalleAnuncio = "https://api.fotocasa.es/PropertySearch/Property?culture=es-ES&locale=es-ES&transactionType=" +
                        idContrato + "&periodicityId=0&id=" + idAnuncio;


                HttpClient client = HttpClients.custom().build();
                RequestBuilder requestBuilder = RequestBuilder.get().setUri(urlDetalleAnuncio);

                HttpUriRequest request = requestBuilder.build();

                try {
                    HttpResponse response = client.execute(request);
                    String html = EntityUtils.toString(response.getEntity());

                    JSONObject root = (JSONObject) new JSONParser().parse(html);

                    return root;

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return new JSONObject();
            }
        };

        List<JSONObject> jsons  = datos.keySet().stream()
                                    .map(key -> {

                                        Integer tipoContrato = convertirTipoContrato2Int.apply(datos.get(key));

                                        JSONObject res = new JSONObject();

                                        if (tipoContrato != null){
                                            res = obtenerDatosDetalleAnuncio.apply(new Par<>(key, tipoContrato));
                                            esperar(1000);
                                        }

                                        return res;
                                    }).collect(Collectors.toList());

        guardarJsons2Archivo(RUTA_JSONS_MODO_PRUEBA, false, jsons);
    }
}
