package controlador.seguridad;

import controlador.managers.ControladorUsuario;
import io.javalin.http.Context;
import modelo.pojo.Usuario;
import org.json.simple.JSONObject;
import org.tinylog.Logger;
import utilidades.Constantes;
import utilidades.HTTPCodes;
import utilidades.Par;
import utilidades.SecurityUtils;

/**
 * El securata se encargara de proporcionar los tokens a los usuarios
 * que se logueen
 */
public class Securata {

    private Context ctx;
    private JSONObject peticion;
    private Usuario usuarioTemp;
    private Usuario usuarioReal;

    private boolean activo = true;

    private ControladorUsuario controladorUsuario;
    private SecurityUtils securityUtils;
    private TokensManejador tokensManejador;

    public Securata(Context ctx, JSONObject peticion){
        this(ctx, peticion, Usuario.fromJson(peticion), null);
    }

    public Securata(Context ctx, JSONObject peticion, Usuario usuarioReal){
        this(ctx, peticion, Usuario.fromJson(peticion), usuarioReal);
    }

    public Securata(Context ctx, JSONObject peticion, Usuario usuarioTemp, Usuario usuarioReal){
        this.controladorUsuario = new ControladorUsuario();
        this.ctx = ctx;
        this.peticion = peticion;
        this.usuarioTemp = usuarioTemp;
        this.usuarioReal = usuarioReal;
        this.securityUtils = new SecurityUtils();
        this.tokensManejador = new TokensManejador();
        init();
    }

    private void init() {

        // Si no tenemos los datos del usuario en la base de datos, no hay login poosible
        if (usuarioReal == null){
            Par<Integer, Usuario> resultado = controladorUsuario.buscarUsuarioPorCorreo(usuarioTemp.getCorreo());
            int estado = resultado.getPrimero();

            JSONObject respuesta = new JSONObject();

            // Ocurrio un error en el servidor
            if (estado == 1){
                ctx.status(HTTPCodes._500.getCodigo());
                respuesta.put(Constantes.RESPUESTA_KEY_MSG, "Ocurrio un error en el servidor");
                ctx.json(respuesta);
                activo = false;
                return;
            }

            // No existe usuario en la base de datos con ese correo
            else if (estado == 2){
                ctx.status(HTTPCodes._401.getCodigo());
                respuesta.put(Constantes.RESPUESTA_KEY_MSG, "Compruebe que los datos introducidos son correctos");
                ctx.json(respuesta);
                activo = false;
                return;
            }

            // El usuario existe en la base de datos
            usuarioReal = resultado.getSegundo();
        }

    }

    public void loguea(){

        // Solo si el securata esta activo realizara la accion
        if (activo){

            JSONObject respuesta = new JSONObject();

            boolean contraseniasCoinciden = securityUtils.verificarContrasenia(usuarioTemp.getContrasenia(), usuarioReal.getContrasenia());

            // Comprobamos que las contraseñas coincidan
            if (!contraseniasCoinciden){
                ctx.status(HTTPCodes._401.getCodigo());
                respuesta.put(Constantes.RESPUESTA_KEY_MSG, "Compruebe que los datos introducidos son correctos");
                ctx.json(respuesta);
                activo = false;
                return;
            }

            // Creamos el token del usuario
            String tokenUsuario = tokensManejador.crearTokenParaUsuario(usuarioReal);

            // Ocurrio un erorr al crear el token
            if (tokenUsuario == null){
                ctx.status(HTTPCodes._500.getCodigo());
                respuesta.put(Constantes.RESPUESTA_KEY_MSG, "Compruebe que los datos introducidos son correctos");
                ctx.json(respuesta);
                activo = false;
                return;
            }

            ctx.status(HTTPCodes._200.getCodigo());
            respuesta.put(Constantes.RESPUESTA_KEY_TOKEN, tokenUsuario);
            ctx.json(respuesta);
        }
    }
}
