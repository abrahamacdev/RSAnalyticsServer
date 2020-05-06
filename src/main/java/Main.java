import controlador.Server;
import controlador.seguridad.TokensManejador;

public class Main {

    public static void main(String[] args) {

        // Cargamos las claves del servidor
        TokensManejador.init();

        // Inicializamos el servidor
        new Server();
    }
}