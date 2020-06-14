package com.rsanalytics.utilidades.rest;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.rsanalytics.utilidades.Par;

import java.security.SecureRandom;

import static com.rsanalytics.utilidades.Constantes.*;
import static com.rsanalytics.utilidades.Constantes.ESTRATEGIA_CONTRASENIAS_LARGAS;

public class SecurityUtils {

    public static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Ciframos la contraseña proporcionada usando el algoritmo BCrypt
     * @param contrasenia
     * @return
     */
    public Par<byte[], byte[]> cifrarContrasenia(byte[] contrasenia){

        byte[] salt = new byte[16];

        // Generamos un salt aleatorio de 16 bytes
        synchronized (secureRandom){
            secureRandom.nextBytes(salt);
        }

        // Hasheamos la contrasenia
        byte[] contraseniaSalteada = BCrypt.with(VERSION_BCRYPT, ESTRATEGIA_CONTRASENIAS_LARGAS)
                .hash(COSTO_CIFRADO_BCRYPT, salt, contrasenia);

        return new Par<>(contraseniaSalteada, salt);
    }

    /**
     * Verificamos si la contrasenia con la que se intenta autenticar un usuario se corresponde
     * con la actual
     * @param raw
     * @param realPassword
     * @return
     */
    public boolean verificarContrasenia(byte[] raw, byte[] realPassword){

        BCrypt.Result result = BCrypt.verifyer(VERSION_BCRYPT, ESTRATEGIA_CONTRASENIAS_LARGAS).verify(raw, realPassword);
        return result.verified;
    }

}
