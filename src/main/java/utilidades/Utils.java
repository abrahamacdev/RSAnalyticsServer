package utilidades;

import com.sun.xml.fastinfoset.util.CharArray;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.tinylog.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utilidades.Constantes.*;

public class Utils {

    private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("RSAnalytics");

    public static EntityManager crearEntityManager(){
        synchronized (entityManagerFactory){
            if (entityManagerFactory.isOpen()){
                return entityManagerFactory.createEntityManager();
            }
            return null;
        }
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
}
