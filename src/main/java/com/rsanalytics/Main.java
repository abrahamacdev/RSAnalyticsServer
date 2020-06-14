package com.rsanalytics;

import com.rsanalytics.controlador.Server;
import com.rsanalytics.controlador.seguridad.TokensManejador;
import com.rsanalytics.utilidades.PrimeraVez;

public class Main {

    public static void main(String[] args) {

        // Creamos los directorios que necesita para funcionar el sistema
        PrimeraVez.init();

        // Cargamos las claves del servidor
        TokensManejador.init();

        // Inicializamos el servidor
        new Server();
    }
}