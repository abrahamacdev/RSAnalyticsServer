package controlador.rest.handlers;

import controlador.managers.ControladorGrupo;
import controlador.managers.ControladorUsuario;
import controlador.seguridad.Securata;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import modelo.pojo.Grupo;
import modelo.pojo.Usuario;
import modelo.pojo.usuario_grupo.UsuarioGrupo;
import org.hibernate.Hibernate;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utilidades.Constantes;
import utilidades.HTTPCodes;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
        /*app.get(Constantes.REST.GRUPO.GRUPO_PATH.value + Constantes.REST.GRUPO.BUSCAR_ENDPOINT.value + "/:nombre",
                (ctx) -> ejecutar(this::datosGeneralesGrupo, ctx));*/

        // Obtencion de los datos del grupo
        app.get(Constantes.REST.GRUPO.GRUPO_PATH.value + Constantes.REST.GRUPO.DATOS_GRUPO_ENDPOINT.value,
                (ctx -> ejecutar(this::datosGeneralesGrupo, ctx)));

        // Abandono de un grupo
        app.post(Constantes.REST.GRUPO.GRUPO_PATH.value + Constantes.REST.GRUPO.ABANDONAR_ENDPOINT.value,
                (ctx) -> ejecutar(this::abandonarGrupo, ctx));
    }

    /* ----- Creacion de un nuevo grupo ----- */
    private boolean creacionGrupoTieneCamposNecesarios(JSONObject jsonObject){

        // No tiene el nombre del grupo
        if (!jsonObject.containsKey("nombreGrupo")){
            return false;
        }

        return true;
    }

    private void realizarRegistroGrupo(Context ctx, JSONObject cuerpo, JSONObject respuesta, Jwt token){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        ControladorGrupo controladorGrupo = new ControladorGrupo();
        ControladorUsuario controladorUsuario = new ControladorUsuario();

        Claims claims = (Claims) token.getBody();
        String correo = claims.getSubject();

        // Obtenemoos el oobjeto usuario de la base de datos
        Par<Integer, Usuario> usuarioError = controladorUsuario.buscarUsuarioPorCorreo(correo);
        int codigoUsuarioError = usuarioError.getPrimero();

        // Comprobamos si ocurrio algun problema
        switch (codigoUsuarioError){

            // Occurrio un error
            case 1:
            case 2:
                ctx.status(HTTPCodes._500.getCodigo());
                respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value,"Ocurrio un error");
                ctx.result(respuesta.toJSONString());
                return;
        }



        //Comprobamoso si existe algun grupo con ese nombre en la base de datos
        String nombreGrupo = (String) cuerpo.get("nombreGrupo");

        Par<Integer, Grupo> grupoError = null;
        int codigoGrupoError;

        // Comproobamos que el nombre del grupo sea valido
        if (nombreGrupo.trim().length() > 0){
            grupoError = controladorGrupo.buscarGrupoPorNombre(nombreGrupo);
            codigoGrupoError = grupoError.getPrimero();
        }

        else {
            ctx.status(HTTPCodes._400.getCodigo());
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value,"El noombre del grupo no es validos");
            ctx.result(respuesta.toJSONString());
            return;
        }

        switch (codigoGrupoError){

            // Existe un grupo en la base de datos
            case 0:
                ctx.status(HTTPCodes._409.getCodigo());
                respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value,"Ya existe un grupo con ese nombre");
                ctx.result(respuesta.toJSONString());
                return;

            // Ocurrio un error desconoocido
            case 2:
                ctx.status(HTTPCodes._500.getCodigo());
                respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value,"Ocurrio un error");
                ctx.result(respuesta.toJSONString());
                return;
        }

        // Comprobamos que el usuario no pertenezca ya a un grupo
        Par<Integer, Grupo> resultadoBusquedaGruposUs = controladorGrupo.buscarGrupoDelUsuarioConCorreo(correo);
        int codigoBusquedaGrupoUs = resultadoBusquedaGruposUs.getPrimero();

        switch (codigoBusquedaGrupoUs){
            // Pertenece a un grupo
            case 0:
            case 2:
                ctx.status(HTTPCodes._403.getCodigo());
                respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value,"El usuario ya pertenece a un grupo");
                ctx.result(respuesta.toJSONString());
                return;

            // Ocurrio un error desconocido
            case 3:
                ctx.status(HTTPCodes._500.getCodigo());
                respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value,"Ocurrio un error");
                ctx.result(respuesta.toJSONString());
                return;
        }

        // Creamos el nuevo objeto Grupo
        Grupo nuevoGrupo = new Grupo(nombreGrupo, usuarioError.getSegundo());

        // Añadimos al usuario creado a la lista de miembros del grupo
        UsuarioGrupo usuarioGrupo = new UsuarioGrupo(usuarioError.getSegundo(), nuevoGrupo);
        nuevoGrupo.getUsuarios().add(usuarioGrupo);

        // Comenzamos una transaccion para asegurar la atomicidad de la operacion
        entityTransaction.begin();

        // Guardamos el nuevo grupo en la base de datos
        Par<Exception, Grupo> resultadoGuardadoGrupo = controladorGrupo.guardarNuevoGrupo(nuevoGrupo, entityManager);

        // Comprobamos que nno ocurriese ningun error durante la persistencia de los datos
        if (resultadoGuardadoGrupo.getPrimero() != null){
            entityTransaction.rollback();
            ctx.status(HTTPCodes._500.getCodigo());
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value,"Ocurrio un error");
            ctx.result(respuesta.toJSONString());
            entityManager.close();
            return;
        }

        // Aseguramos la operacion
        entityTransaction.commit();
        entityManager.close();

        // Se ha creado el grupo exitosamente
        ctx.status(HTTPCodes._201.getCodigo());
        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value,"Se ha creado el grupo exitosamente");
        ctx.result(respuesta.toJSONString());
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
                        realizarRegistroGrupo(ctx, json, respuesta, token);
                    }


                } catch (ParseException e) {
                    e.printStackTrace();
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
    private JSONObject crearResumenGeneralDatosGrupo(Grupo grupo){

        JSONObject resumen = new JSONObject();

        resumen.put("nombre", grupo.getNombre());

        JSONArray miembros = new JSONArray();

        grupo.getUsuarios().forEach((usuarioGrupo) -> {
            Usuario usuario = usuarioGrupo.getUsuario();

            JSONObject datosMiembro = new JSONObject();
            datosMiembro.put("nombre", usuario.getNombre() + " " + usuario.getPrimerApellido() + " " + usuario.getSegundoApellido());
            datosMiembro.put("miembroDesde", usuarioGrupo.getFechaIngreso().toInstant().toEpochMilli());
            datosMiembro.put("genero", usuario.getGenero());

            miembros.add(datosMiembro);
        });

        resumen.put("miembros", miembros);

        return resumen;
    }

    private Runnable datosGeneralesGrupo(Context ctx){

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                Jwt token = new Securata(ctx).validarYRetornarToken();
                if (token != null){

                    JSONObject respuesta = new JSONObject();

                    ControladorGrupo controladorGrupo = new ControladorGrupo();

                    String correo = ((Claims) token.getBody()).getSubject();

                    // Necesitamos manejar la sesion para inicializar la listta de usuarios nosotros mismos
                    EntityManager entityManager = Utils.crearEntityManager();

                    Par<Integer, Grupo> resultadoBusquedaGrupo = controladorGrupo.buscarGrupoDelUsuarioConCorreo(correo, entityManager);
                    int codBusquedaGrupoo = resultadoBusquedaGrupo.getPrimero();

                    switch (codBusquedaGrupoo){

                        // El usuario no pertenece a ningun grupo
                        case 1:
                            entityManager.close();
                            ctx.status(HTTPCodes._400.getCodigo());
                            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "El usuario no pertenece a ningun grupo");
                            ctx.result(respuesta.toJSONString());
                            return;

                        // Ocurrio un error desconocido o el usuario pertenece a muchos grupos
                        case 2:
                        case 3:
                            entityManager.close();
                            ctx.status(HTTPCodes._500.getCodigo());
                            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error");
                            ctx.result(respuesta.toJSONString());
                            return;
                    }

                    Grupo grupo = resultadoBusquedaGrupo.getSegundo();
                    Hibernate.initialize(grupo.getUsuarios()); // Inicializa la lista de usuarios del grupo

                    // Una vez inicializados los usuarios del grupo, cerramos la sesion
                    entityManager.close();

                    JSONObject resumenDatosGrupos = crearResumenGeneralDatosGrupo(grupo);

                    // Enviamos el resumen con los datos generales del grupo
                    ctx.status(HTTPCodes._200.getCodigo());
                    respuesta.put(Constantes.REST.RESPUESTAS_KEYS.GRUPO.value, resumenDatosGrupos);
                    ctx.result(respuesta.toJSONString());
                    
                }
            }
        };

        return runnable;
    }
    /* -------------------------------------- */


    /* ----- Abandono de un grupo ----- */
    private Runnable abandonarGrupo(Context ctx){

        return new Runnable() {
            @Override
            public void run() {

                Jwt token = new Securata(ctx).validarYRetornarToken();

                if (token != null){

                    JSONObject respuesta = new JSONObject();

                    String correo = ((Claims) token.getBody()).getSubject();

                    ControladorUsuario controladorUsuario = new ControladorUsuario();
                    Par<Integer,Usuario> resultadoBusquedaUsuario = controladorUsuario.buscarUsuarioPorCorreo(correo);
                    int codBusquedaUsuario = resultadoBusquedaUsuario.getPrimero();

                    switch (codBusquedaUsuario){

                        // Ocurrio un error
                        case 1:
                            ctx.status(HTTPCodes._500.getCodigo());
                            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error");
                            ctx.result(respuesta.toJSONString());
                            return;

                        case 2:
                            ctx.status(HTTPCodes._400.getCodigo());
                            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "El usuario no existe");
                            ctx.result(respuesta.toJSONString());
                            return;
                    }

                    ControladorGrupo controladorGrupo = new ControladorGrupo();
                    int resultadoEliminacion = controladorGrupo.eliminarDelGrupoAlUsuario(correo);

                    // Ocurrio un error desconocido
                    if (resultadoEliminacion == 3){
                        ctx.status(HTTPCodes._500.getCodigo());
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error");
                        ctx.result(respuesta.toJSONString());
                        return;
                    }

                    // El usuario no pertenecia a ningun grupo
                    else if (resultadoEliminacion == 2){
                        ctx.status(HTTPCodes._404.getCodigo());
                        return;
                    }

                    // Se expulso del grupo o se elimino el grupo por completo exitosamente
                    ctx.status(HTTPCodes._200.getCodigo());
                }
            }
        };
    }
    /* -------------------------------- */
}
