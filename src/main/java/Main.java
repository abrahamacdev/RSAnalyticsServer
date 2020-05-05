import controlador.Server;
import controlador.managers.ControladorAnuncio;
import controlador.seguridad.TokensManejador;
import modelo.pojo.scrapers.Anuncio;
import modelo.pojo.scrapers.Procedencia;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        // Cargamos las claves del servidor
        TokensManejador.init();

        // Inicializamos el servidor
        new Server();
    }
}