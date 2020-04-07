package controlador.seguridad;

import controlador.managers.UsuarioManager;
import io.javalin.http.Context;
import modelo.pojo.Usuario;
import org.json.simple.JSONObject;
import utilidades.Constantes;
import utilidades.HTTPCodes;
import utilidades.Par;

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

    private UsuarioManager usuarioManager;

    public Securata(Context ctx, JSONObject peticion){
        this(ctx, peticion, Usuario.fromJson(peticion), null);
    }

    public Securata(Context ctx, JSONObject peticion, Usuario usuarioReal){
        this(ctx, peticion, Usuario.fromJson(peticion), usuarioReal);
    }

    public Securata(Context ctx, JSONObject peticion, Usuario usuarioTemp, Usuario usuarioReal){
        this.usuarioManager = new UsuarioManager();
        this.ctx = ctx;
        this.peticion = peticion;
        this.usuarioTemp = usuarioTemp;
        this.usuarioReal = usuarioReal;
        init();
    }

    private void init() {

        // Si no tenemos los datos del usuario en la base de datos, no hay login poosible
        if (usuarioReal == null){
            Par<Usuario, Exception> parUsuarioExcepcion = usuarioManager.buscarUsuarioPorCorreo(usuarioTemp.getCorreo());

            JSONObject respuesta = new JSONObject();

            // No eexiste usuario enn la base de datos con eese correo
            if (parUsuarioExcepcion.getPrimero() == null && parUsuarioExcepcion.getSegundo() == null){
                ctx.status(HTTPCodes._400.getCodigo());
                respuesta.put(Constantes.RESPUESTA_MSG_KEY, "Compruebe que los datos introducidos son correctos");
                ctx.json(respuesta);
                activo = false;
                return;
            }

            // Ocurrio un error en el servidor
            else if (parUsuarioExcepcion.getSegundo() != null){
                ctx.status(HTTPCodes._500.getCodigo());
                respuesta.put(Constantes.RESPUESTA_MSG_KEY, "Ocurrio un error en el servidor");
                ctx.json(respuesta);
                activo = false;
                return;
            }

            // El usuario existe en la base de datos
            usuarioReal = parUsuarioExcepcion.getPrimero();
        }

    }

    public Securata loguea(){

        // Solo si el securata esta activo realizara la accion
        if (activo){

        }

        return this;
    }
}
