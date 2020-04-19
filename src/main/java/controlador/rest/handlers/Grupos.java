package controlador.rest.handlers;

import controlador.managers.ControladorGrupo;
import controlador.managers.ControladorUsuario;
import controlador.seguridad.Securata;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import modelo.pojo.Usuario;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utilidades.Constantes;
import utilidades.HTTPCodes;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.concurrent.ExecutorService;

public class Grupos extends AbstractHandler{

    public Grupos(Javalin app, ExecutorService piscina) {
        super(app, piscina);
    }

    @Override
    protected void registrarHandlers(){

        // Registro de un nuevo grupo
        app.post(Constantes.REST.GRUPO.GRUPO_PATH.value + Constantes.REST.GRUPO.REGISTRO_ENDPOINT.value,
                (ctx) -> ejecutar(this::crearGrupo, ctx));

        // Busqueda de un grupo
        app.get(Constantes.REST.GRUPO.GRUPO_PATH.value + Constantes.REST.GRUPO.BUSCAR_ENDPOINT.value + "/:nombre",
                (ctx) -> ejecutar(this::buscarGrupo, ctx));

    }

    /* ----- Creacion de un nuevo grupo ----- */
    private boolean creacionGrupoTieneCamposNecesarios(JSONObject jsonObject){

        // No tiene el nombre del grupo
        if (!jsonObject.containsKey("nombreGrupo")){
            return false;
        }

        return true;
    }

    private void realizarRegistroGrupo(Context ctx, JSONObject respuesta, Jwt token){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        ControladorGrupo controladorGrupo = new ControladorGrupo();
        ControladorUsuario controladorUsuario = new ControladorUsuario();

        Claims claims = (Claims) token.getBody();
        String correo = claims.getSubject();

        // Obtenemoos el oobjeto usuario de la base de datos
        Par<Integer, Usuario> usuarioError = controladorUsuario.buscarUsuarioPorCorreo(correo);
        int codigoUsuarioError = usuarioError.getPrimero();

        // Ocurrio un error desconocido
        if (codigoUsuarioError == 1){
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value,"Ocurrio un error");
        }

        
        else if (codigoUsuarioError == 2){
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value,"Ocurrio un error");
        }

        entityTransaction.begin();


    }

    private Runnable crearGrupo(Context ctx){

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                JSONObject respuesta = new JSONObject();

                try {

                    JSONObject json  = (JSONObject) new JSONParser().parse(ctx.body());

                    // No contiene los campos necesarios
                    if (!creacionGrupoTieneCamposNecesarios(json)){
                        ctx.status(HTTPCodes._400.getCodigo());
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Faltan campos");
                        ctx.json(respuesta.toJSONString());
                        return;
                    }

                    // Validamos el token
                    Jwt token = new Securata(ctx).validarYRetornarToken();
                    if (token != null){
                        realizarRegistroGrupo(ctx, respuesta, token);
                    }


                } catch (ParseException e) {
                    ctx.status(HTTPCodes._500.getCodigo());
                    respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error inesperado");
                    ctx.json(respuesta.toJSONString());
                    return;
                }
            }
        };

        return runnable;
    }
    /* -------------------------------------- */


    /* ----- Comprobar existencia de un grupo ----- */
    private Runnable buscarGrupo(Context ctx){

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

            }
        };

        return runnable;
    }
    /* -------------------------------------- */

}
