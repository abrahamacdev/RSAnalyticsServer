package controlador.seguridad;

import controlador.managers.ControladorToken;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.DeserializationException;
import io.jsonwebtoken.io.Deserializer;
import modelo.pojo.Token;
import modelo.pojo.Usuario;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.tinylog.Logger;
import utilidades.Constantes;
import utilidades.Par;
import utilidades.Propiedades;

import java.io.FileInputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Map;
import java.util.UUID;

public class TokensManejador {

    private static KeyPair rsaKeys;

    private ControladorToken controladorToken;

    // Cargamos las claves privadas y publicas
    public static void init(){
        if (rsaKeys == null){
            try {
                KeyStore keyStore = KeyStore.getInstance("JKS");
                char[] password = ((String) Propiedades.getProperties().get(Constantes.PROP_KEYSTORE_PASS)).toCharArray();
                String rutaArchivo = Propiedades.getProperties().getProperty(Constantes.PROP_KEYSTORE_PATH);
                keyStore.load(new FileInputStream(rutaArchivo), password);

                Enumeration<String> enumeration = keyStore.aliases();
                String alias = enumeration.nextElement();

                if (alias != null) {

                    // Obtenemos la clave privada
                    Key key = keyStore.getKey(alias, password);

                    // Obtenemos la clave publica
                    Certificate certificate = keyStore.getCertificate(alias);
                    PublicKey publicKey = certificate.getPublicKey();

                    // Creamos un par de claves con ambas
                    rsaKeys = new KeyPair(publicKey, (PrivateKey) key);

                }
            } catch (Exception e){
                Logger.error(e,"Ocurrio un error al cargar las claves del servidor");
            }
        }
    }

    public TokensManejador(){
        this.controladorToken = new ControladorToken();
    }

    /**
     * Creamos un token valido para un usuario con un tiempo de expiracion
     * de {@value utilidades.Constantes#TIEMPO_EXPIRACION_TOKEN_ACCESO} minutos
     * @param usuario
     * @return
     */
    public String crearTokenParaUsuario(Usuario usuario){

        Par<Long, Long> fechasToken = generarFechasToken();
        String idToken = UUID.randomUUID().toString();

        // Creamos el objeto token que persistiremos en la base de datos
        Token token = new Token();
        token.setUsuario(usuario);
        token.setIdPublico(idToken);

        Par<Integer, Token> tokenGuardado = controladorToken.guardarNuevoToken(token);

        // Occurio un erro al crear el token
        if (tokenGuardado.getPrimero() == 1){
            Logger.warn("No se ha podido crear un token de acceso para un usuario");
            return null;
        }

        // Creamos el cuerpo del token
        JSONObject payload = new JSONObject();
        payload.put(Constantes.JWT_KEY_CREADOR, Constantes.NOMBRE_APP);
        payload.put(Constantes.JWT_KEY_FECHA_CREACION, fechasToken.getPrimero());
        payload.put(Constantes.JWT_KEY_FECHA_EXPIRACION, fechasToken.getSegundo());
        payload.put(Constantes.JWT_KEY_ID_TOKEN, idToken);
        payload.put(Constantes.JWT_KEY_ADMIN, usuario.getRol().esAdmin());
        payload.put(Constantes.JWT_KEY_SUJETO, usuario.getCorreo());


        // Creamos el token que devolveremos al usuario
        String jwt =  Jwts.builder()
                .setPayload(payload.toJSONString())
                .signWith(rsaKeys.getPrivate(), SignatureAlgorithm.RS384)
                .compact();

        return jwt;
    }

    /**
     * Coomprobamos si un token es valido y lo retornamos ya parseado
     * @param token
     * @return  0,token -> Token valido y parseado
     *          1,null -> NO se ha podido verificar el token
     *          2,null -> Ocurrio otro error desconocido
     */
    public Par<Integer, Jwt> comprobarValidezToken(String token){

        try {

            Jwt parsedToken = Jwts.parserBuilder()
                    .setSigningKey(rsaKeys.getPublic())
                    .build()
                    .parseClaimsJws(token);

            return new Par<>(0, parsedToken);

        }catch (JwtException e){
            return new Par<>(1, null);
        }catch (Exception e){
            return new Par<>(2,null);
        }
    }

    private Par<Long, Long> generarFechasToken(){

        long actual = System.currentTimeMillis();
        long duracion = Constantes.TIEMPO_EXPIRACION_TOKEN_ACCESO *  60 * 1000; // min -> seg -> ms
        long expiration = actual + duracion;

        return new Par<Long, Long>(actual, expiration);
    }
}
