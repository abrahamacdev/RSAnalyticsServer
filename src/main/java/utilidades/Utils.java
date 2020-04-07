package utilidades;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.security.SecureRandom;

import static utilidades.Constantes.*;

public class Utils {

    private static SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();

    /**
     * Creamos una nueva sesion con la interactuaremos con la base de
     * datos
     * @return Session recien creada
     */
    public static Session crearNuevaSesion(){
        synchronized (sessionFactory){
            if (sessionFactory.isOpen()){
                return sessionFactory.openSession();
            }
            return null;
        }
    }

    /**
     * Cerramos el objeto SessionFactory con el creamos
     * los oobjectos Session
     * @return
     */
    public static boolean cerrarSessionFactory(){
        synchronized (sessionFactory){
            if (sessionFactory.isOpen()){
                sessionFactory.close();
                return true;
            }
            return false;
        }
    }

    /**
     * Ciframos la contraseña proporcionada usando el algoritmo BCrypt
     * @param contrasenia
     * @return
     */
    public static synchronized Par<byte[], byte[]> cifrarContrasenia(byte[] contrasenia){

        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);

        byte[] contraseniaSalteada = BCrypt.with(VERSION_BCRYPT, ESTRATEGIA_CONTRASENIAS_LARGAS)
                .hash(COSTO_CIFRADO_BCRYPT, salt, contrasenia);

        return new Par<>(contraseniaSalteada, salt);
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
}