package controlador.rest.handlers;

import com.sun.xml.bind.v2.runtime.reflect.opt.Const;
import controlador.managers.ControladorGrupo;
import controlador.managers.ControladorNotificacion;
import controlador.managers.ControladorUsuario;
import controlador.seguridad.Securata;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import modelo.pojo.*;
import modelo.pojo.usuario_grupo.UsuarioGrupo;
import org.hibernate.Hibernate;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.tinylog.Logger;
import utilidades.Constantes;
import utilidades.HTTPCodes;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

public class Grupos extends AbstractHandler{

    public Grupos(Javalin app, ExecutorService piscina) {
        super(app, piscina);
    }

    @Override
    protected void registrarHandlers(){

        // Registro de un nuevo grupo
        app.post(Constantes.REST.GRUPO.GRUPO_PATH.value + Constantes.REST.GRUPO.REGISTRO_ENDPOINT.value,
                (ctx) -> ejecutar(this::crearGrupo, ctx));

        // Obtencion de los datos del grupo
        app.get(Constantes.REST.GRUPO.GRUPO_PATH.value + Constantes.REST.GRUPO.DATOS_GRUPO_ENDPOINT.value,
                (ctx -> ejecutar(this::datosGeneralesGrupo, ctx)));

        // Abandono de un grupo
        app.post(Constantes.REST.GRUPO.GRUPO_PATH.value + Constantes.REST.GRUPO.ABANDONAR_ENDPOINT.value,
                (ctx) -> ejecutar(this::abandonarGrupo, ctx));

        // Invitar a un usuario
        app.post(Constantes.REST.GRUPO.GRUPO_PATH.value + Constantes.REST.GRUPO.INVITAR_ENDPOINT.value,
                (ctx) -> ejecutar(this::invitar, ctx));

        // Aceptar invitacion de adhesion a grupo
        app.post(Constantes.REST.GRUPO.GRUPO_PATH.value + Constantes.REST.GRUPO.INVITACION_PATH.value + Constantes.REST.GRUPO.ACEPTAR_INVITACION_ENDPOINT.value,
                (ctx) -> ejecutar(this::aceptarInvitacion, ctx));

        // Rechazo invitacion de adhesion a grupo
        app.post(Constantes.REST.GRUPO.GRUPO_PATH.value + Constantes.REST.GRUPO.INVITACION_PATH.value + Constantes.REST.GRUPO.RECHAZAR_INVITACION_ENDPOINT.value,
                ctx -> ejecutar(this::rechazarInvitacion, ctx));
    }


    // ----- Creacion de un nuevo grupo -----
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
    // --------------------------------------


    // ----- Comprobar existencia de un grupo -----
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
    // --------------------------------------


    // ----- Abandono de un grupo -----
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
    // --------------------------------


    // ----- Invitacion a un grupo -----
    private boolean invitacionContieneCamposNecesarios(JSONObject cuerpo){

        if (!cuerpo.containsKey("invitado")){
            return false;
        }

        return true;
    }

    /**
     * Realizaremos unas comprobaciones previas sobre los datos enviados en la peticion
     * @param cuerpoToken
     * @param cuerpoPeticion
     * @return  0,null -> Todo esta correcto, se puede realizar la invitacion
     *          x,y -> Algo salio mal
     */
    private Par<Integer, String> comprobacionesPreviasInvitacion(Claims cuerpoToken, JSONObject cuerpoPeticion){

        String invitado = (String) cuerpoPeticion.get("invitado");
        String responsable = cuerpoToken.getSubject();

        // El invitado es el propio responsable
        if (invitado.equals(responsable)){
            return new Par<>(HTTPCodes._400.getCodigo(), "No puedes invitarte a ti mismo");
        }

        // La peticion no tiene los campos necesarios
        if (!invitacionContieneCamposNecesarios(cuerpoPeticion)){
            return new Par<>(HTTPCodes._400.getCodigo(),"Faltan campos por rellenar");
        }

        // Comprobamos que el "invitador" sea responsable de algun grupo
        ControladorGrupo controladorGrupo = new ControladorGrupo();
        int codResultBusquedaUsResponsable = controladorGrupo.elUsuarioEsResponsableDeGrupo(responsable);
        switch (codResultBusquedaUsResponsable){

            case 1:
                return new Par<>(HTTPCodes._404.getCodigo(), "Solo los responsables de grupo pueden invitar a otros usuarios");

            case 3:
                return new Par<>(HTTPCodes._500.getCodigo(), "Ocurrio un error");
        }

        // Comprobamos que el "invitado" exista
        ControladorUsuario controladorUsuario = new ControladorUsuario();
        Par<Integer, Usuario> resultadoBusquedaUsuario = controladorUsuario.buscarUsuarioPorCorreo((String) cuerpoPeticion.get("invitado"));
        int codResulBusquedaUsuario = resultadoBusquedaUsuario.getPrimero();
        switch (codResulBusquedaUsuario){

            case 1:
                return new Par<>(HTTPCodes._500.getCodigo(),"Ocurrio un error");

            case 2:
                return new Par<>(HTTPCodes._404.getCodigo(),"El usuario no existe");
        }

        // Obtenemos el grupo al que pertenece el responsable
        Par<Integer, Grupo> resBusGrupoResponsable = controladorGrupo.buscarGrupoDelUsuarioConCorreo(responsable);
        int codBusGrupoResponsable = resBusGrupoResponsable.getPrimero();
        if (codBusGrupoResponsable != 0 ){
            return new Par<>(HTTPCodes._500.getCodigo(), "Ocurrio un error");
        }

        // Comprobaremos que el usuario no tenga una invitacion pendiente de union al grupo del responsable
        ControladorNotificacion controladorNotificacion = new ControladorNotificacion();
        Par<Integer, List<Notificacion>> resultadoBusNotiUsuario = controladorNotificacion.obtenerInvitacionesGrupoDelUsuario(invitado);
        int codResultBusNotiUsuario = resultadoBusNotiUsuario.getPrimero();
        if (codResultBusNotiUsuario == 2){
            return new Par<>(HTTPCodes._500.getCodigo(), "Ocurrio un error");
        }

        else if(codResultBusNotiUsuario == 0){

            Grupo grupoDelResponsable = resBusGrupoResponsable.getSegundo();
            List<Notificacion> notificaciones = resultadoBusNotiUsuario.getSegundo();

            // Comprobamos si hay invitaciones al grupo del "invitador" que no hallan sido aun respondidas
            Optional<Notificacion> invitacion = notificaciones.stream()
                    .filter((notificacion -> notificacion.getAccion().getGrupo().equals(grupoDelResponsable) && !notificacion.getAccion().isCompletada()))
                    .findFirst();
            if (invitacion.isPresent()){
                return new Par<>(HTTPCodes._400.getCodigo(), "Hay una invitacion pendiente");
            }
        }

        // Comprobamos que el invitado no pertenezca ya al grupo del invitador
        Par<Integer,Grupo> resBusGrupoInvitado = controladorGrupo.buscarGrupoDelUsuarioConCorreo(invitado);
        int codResultBusGrupoInvitado = resBusGrupoInvitado.getPrimero();
        if (codResultBusGrupoInvitado > 1){
            return new Par<>(HTTPCodes._500.getCodigo(), "Ocurrio un error");
        }
        else if (codResultBusGrupoInvitado == 0 && resBusGrupoInvitado.getSegundo().equals(resBusGrupoResponsable.getSegundo())){
            return new Par<>(HTTPCodes._400.getCodigo(),"El usuario ya pertenece al grupo");
        }

        // Toodo esta correcto
        return new Par<>(-1,null);
    }

    private Par<Integer, String> realizarInvitacion(Claims cuerpoToken, JSONObject cuerpoPeticion){

        ControladorUsuario controladorUsuario = new ControladorUsuario();

        String responsable = cuerpoToken.getSubject();
        String invitado = (String) cuerpoPeticion.get("invitado");

        // Obtenemos el objeto usuario que representa al responsable
        Par<Integer,Usuario> resultadoBusquedaResponsable = controladorUsuario.buscarUsuarioPorCorreo(responsable);
        int codResulBusRes = resultadoBusquedaResponsable.getPrimero();
        if (codResulBusRes > 0){
            Logger.error("El responsable no existe u ocurrio un error inesperado (cod " + codResulBusRes + ")");
            return new Par<>(HTTPCodes._500.getCodigo(), "Algo salio mal");
        }

        // Obtenemos el objeto usuario que representa al invitado
        Par<Integer,Usuario> resultadoBusquedaInvitado = controladorUsuario.buscarUsuarioPorCorreo(invitado);
        int codResulBusInv = resultadoBusquedaInvitado.getPrimero();
        if (codResulBusInv > 0){
            Logger.error("El invitado no existe u ocurrio un error inesperado (cod " + codResulBusInv + ")");
            return new Par<>(HTTPCodes._500.getCodigo(), "Algo salio mal");
        }

        // Obtenemos el objeto grupo que representa el grupo al que se esta invitando
        ControladorGrupo controladorGrupo = new ControladorGrupo();
        Par<Integer, Grupo> resultadoBusquedaGrupo = controladorGrupo.buscarGrupoDelUsuarioConCorreo(responsable);
        int codResultBusGrupo = resultadoBusquedaGrupo.getPrimero();
        if (codResultBusGrupo > 0){
            Logger.error("El grupo no existe u ocurrio un error desconocido");
            return new Par<>(HTTPCodes._500.getCodigo(), "Algo salio mal");
        }

        Usuario usuarioEmisor = resultadoBusquedaResponsable.getSegundo();
        Usuario usuarioReceptor = resultadoBusquedaInvitado.getSegundo();

        // Obtenemos el tipo de accion
        Tipo tipo = Tipo.obtenerTipoAccion(Tipo.NOMBRE.INVITACION);

        // Creamos la accion
        Accion accion = new Accion(resultadoBusquedaGrupo.getSegundo(), tipo);

        // Creamos la notificacion
        Notificacion notificacion = new Notificacion(usuarioEmisor, usuarioReceptor, accion);

        //  Realizamos el guardado
        ControladorNotificacion controladorNotificacion = new ControladorNotificacion();
        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        entityTransaction.begin();

        int resuladoGuardado = controladorNotificacion.guardarNuevaNotificacion(notificacion, entityManager);

        // Algo salio mal
        if (resuladoGuardado == 1){
            Logger.error("No se ha podido guardar la notificacion por causas desconocidas");
            entityTransaction.rollback();
            entityManager.close();
            return new Par<>(HTTPCodes._500.getCodigo(), "Algo salio mal");
        }

        // Toodo salio bien
        entityTransaction.commit();
        entityManager.close();

        return new Par<>(HTTPCodes._201.getCodigo(),"Se ha enviado la peticion al usuario");
    }

    private Runnable invitar(Context ctx){
        return new Runnable() {
            @Override
            public void run() {

                Jwt token = new Securata(ctx).validarYRetornarToken();

                if (token != null){

                    JSONObject respuesta = null;

                    try {
                        JSONObject cuerpo = (JSONObject) new JSONParser().parse(ctx.body());

                        Claims tokenClaims = (Claims) token.getBody();

                        // Realizamos las comprobaciones necesarias previas a la realizacion de la invitacion
                        Par<Integer, String> resultadoComprobacionesPrevias = comprobacionesPreviasInvitacion(tokenClaims, cuerpo);
                        if (resultadoComprobacionesPrevias.getPrimero() != -1){
                            respuesta = new JSONObject();
                            ctx.status(resultadoComprobacionesPrevias.getPrimero());
                            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, resultadoComprobacionesPrevias.getSegundo());
                            ctx.result(respuesta.toJSONString());
                            return;
                        }

                        // Realizamos la invitacion
                        Par<Integer, String> resultadoEnvioInvitacion = realizarInvitacion(tokenClaims, cuerpo);
                        respuesta = new JSONObject();
                        ctx.status(resultadoEnvioInvitacion.getPrimero());
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, resultadoEnvioInvitacion.getSegundo());
                        ctx.result(respuesta.toJSONString());
                        return;


                    } catch (ParseException e) {
                        respuesta = new JSONObject();
                        ctx.status(HTTPCodes._500.getCodigo());
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value,"Ocurrio un error inesperado");
                        ctx.result(respuesta.toJSONString());
                        return;
                    }
                }
            }
        };
    }
    // ---------------------------------


    // ------ Aceptar invitacion a grupo -----
    private boolean aceptacionInvitacionContieneCamposNec(JSONObject cuerpo){

        if(!cuerpo.containsKey("idNotificacion")){
            return false;
        }

        return true;
    }

    private Par<Integer, String> realizarComprobacionesPreviasUnionGrupo(Claims token, JSONObject cuerpo){

        String correoUsuario = token.getSubject();
        int idNotificacion = Integer.valueOf((String) cuerpo.get("idNotificacion"));
        ControladorUsuario controladorUsuario = new ControladorUsuario();

        // Comprobamos que el usuario exista en la base datos
        Par<Integer, Usuario> resultadoBusquedaUsuario = controladorUsuario.buscarUsuarioPorCorreo(correoUsuario);
        int codResBusquedaUsuario = resultadoBusquedaUsuario.getPrimero();
        switch (codResBusquedaUsuario){

            case 1:
                return new Par<>(HTTPCodes._500.getCodigo(), "Ocurrio un error");

            case 2:
                return new Par<>(HTTPCodes._400.getCodigo(), "El usuario no existe");
        }

        // Comprobamos que halla una notificacion con el id enviado
        ControladorNotificacion controladorNotificacion = new ControladorNotificacion();
        Par<Integer,Notificacion> resultadoBusquedaNotificacion = controladorNotificacion.obtenerNotificacionConId(idNotificacion);
        int codResBusquedaNotificacion = resultadoBusquedaNotificacion.getPrimero();
        switch (codResBusquedaNotificacion){

            case 1:
                return new Par<>(HTTPCodes._400.getCodigo(), "No existe ninguna notificacion con ese id");

            case 2:
                return new Par<>(HTTPCodes._500.getCodigo(), "Ocurrio un error");
        }

        // Comprobamos la notificacion sea del usuario que ha realizado la peticion
        Usuario usuario = resultadoBusquedaUsuario.getSegundo();
        Notificacion notificacion = resultadoBusquedaNotificacion.getSegundo();
        if (!notificacion.getReceptor().equals(usuario)){
            return new Par<>(HTTPCodes._400.getCodigo(), "Ocurrio un error");
        }

        // La accion ya fue completada
        if (notificacion.getAccion().isCompletada()){
            return new Par<>(HTTPCodes._400.getCodigo(), "Ya respondiste a la notificacion");
        }

        Grupo grupoAUnir = notificacion.getAccion().getGrupo();
        ControladorGrupo controladorGrupo = new ControladorGrupo();
        Par<Integer, Grupo> resBusGrupoInvitado = controladorGrupo.buscarGrupoDelUsuarioConCorreo(correoUsuario);
        int codResBusGrupoInvitado = resBusGrupoInvitado.getPrimero();
        if (codResBusGrupoInvitado > 1){
            return new Par<>(HTTPCodes._500.getCodigo(), "Ocurrio un error");
        }
        else if (codResBusGrupoInvitado == 0){

            Grupo grupoActual = resBusGrupoInvitado.getSegundo();

            // El usuario ya pertenece al grupo al que va a unirse
            if (grupoActual.equals(grupoAUnir)){
                return new Par<>(HTTPCodes._400.getCodigo(), "Ya perteneces al grupo");
            }
        }

        return new Par<>(-1, null);
    }

    private Par<Integer, String> realizarUnionAGrupo(Claims token, JSONObject cuerpo){

        Par<Integer, String> resultadoComprobacionesPrevias = realizarComprobacionesPreviasUnionGrupo(token, cuerpo);
        int codResComprobacionesPrevias = resultadoComprobacionesPrevias.getPrimero();

        // Algo salio mal
        if (codResComprobacionesPrevias != -1){
            return resultadoComprobacionesPrevias;
        }

        String invitado = token.getSubject();
        int idNotificacion = Integer.valueOf((String) cuerpo.get("idNotificacion"));

        ControladorGrupo controladorGrupo = new ControladorGrupo();
        ControladorNotificacion controladorNotificacion = new ControladorNotificacion();
        ControladorUsuario controladorUsuario = new ControladorUsuario();

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        Par<Integer, Notificacion> resBusNotificacionInvitado = controladorNotificacion.obtenerNotificacionConId(idNotificacion, entityManager);
        Par<Integer, Usuario> resBusUsuarioInvitado = controladorUsuario.buscarUsuarioPorCorreo(invitado);

        // Eliminamos al usuario de su anterior grupo (si es que tenia)
        int resAbandonoGrupo = controladorGrupo.eliminarDelGrupoAlUsuario(invitado,entityManager);
        if (resAbandonoGrupo == 3){
            transaction.rollback();
            entityManager.close();
            return new Par<>(HTTPCodes._500.getCodigo(),"Ocurrio un error");
        }


        Notificacion notificacion = resBusNotificacionInvitado.getSegundo();
        Usuario usuarioInvitado = resBusUsuarioInvitado.getSegundo();

        // Añadimos al usuarioo al grupo
        Grupo nuevoGrupo = notificacion.getAccion().getGrupo();
        Hibernate.initialize(nuevoGrupo.getUsuarios()); // Inicializamos la lista
        UsuarioGrupo usuarioGrupo = new UsuarioGrupo(usuarioInvitado,nuevoGrupo);
        nuevoGrupo.getUsuarios().add(usuarioGrupo);

        // Hacemos persistente la relacion
        int resultadoAdhesion = controladorGrupo.actualizarGrupo(nuevoGrupo,entityManager);

        // Algo salio mal
        if (resultadoAdhesion == -1){
            transaction.rollback();
            entityManager.close();
            return new Par<>(HTTPCodes._500.getCodigo(), "Ocurrio un error");
        }

        // Marcamos la accion como realizada
        notificacion.getAccion().setCompletada(true);

        // Hacemos persistentes los cambios en la notificacion
        int resultadoActNoti = controladorNotificacion.actualizarNotificacion(notificacion, entityManager);

        // Algo salio mal
        if (resultadoActNoti == 1){
            transaction.rollback();
            entityManager.close();
            return new Par<>(HTTPCodes._500.getCodigo(), "Ocurrio un error");
        }

        transaction.commit();
        entityManager.close();

        return new Par<>(HTTPCodes._200.getCodigo(), "Se ha unido al grupo correctamente");
    }

    private Runnable aceptarInvitacion(Context ctx){
        return new Runnable() {
            @Override
            public void run() {

                Jwt token = new Securata(ctx).validarYRetornarToken();

                if (token != null){

                    JSONObject respuesta = new JSONObject();

                    try {
                        JSONObject cuerpo = (JSONObject) new JSONParser().parse(ctx.body());

                        // La peticion no contiene los campos necesarios
                        if (!aceptacionInvitacionContieneCamposNec(cuerpo)){
                            ctx.status(HTTPCodes._400.getCodigo());
                            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Faltan campos por rellenar");
                            ctx.result(respuesta.toJSONString());
                            return;
                        }

                        // Realizamos la adhesion al grupo
                        Par<Integer, String> resultado = realizarUnionAGrupo((Claims) token.getBody(), cuerpo);
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, resultado.getSegundo());
                        ctx.status(resultado.getPrimero());
                        ctx.result(respuesta.toJSONString());

                        return;

                    } catch (ParseException e) {
                        Logger.error("Ocurrio un error al parsear la peticion");
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error");
                        ctx.status(HTTPCodes._500.getCodigo());
                        ctx.result(respuesta.toJSONString());
                        return;
                    }
                }
            }
        };
    }
    // ---------------------------------------------


    // ----- Rechazar invitacion a grupo -----
    private boolean rechazoInvitacionContieneCamposNec(JSONObject cuerpo){

        if(!cuerpo.containsKey("idNotificacion")){
            return false;
        }

        return true;
    }

    private Par<Integer, String> realizarComprobacionesPreviasRechazoGrupo(Claims token, JSONObject cuerpo){

        String invitado = token.getSubject();
        int idNotificacion = Integer.valueOf((String) cuerpo.get("idNotificacion"));

        ControladorNotificacion controladorNotificacion = new ControladorNotificacion();
        ControladorUsuario controladorUsuario = new ControladorUsuario();

        // Buscamos la notificacion
        Par<Integer,Notificacion> busNotificacion = controladorNotificacion.obtenerNotificacionConId(idNotificacion);
        int codResBusNotificacion = busNotificacion.getPrimero();
        switch (codResBusNotificacion){

            case 1:
                return new Par<>(HTTPCodes._400.getCodigo(), "No existe ninguna notificacion con ese id");

            case 2:
                return new Par<>(HTTPCodes._500.getCodigo(), "Ocurrio un error");
        }

        // Comprobamos que la notificacion tenga una accion de tipo invitacion
        Notificacion notificacion = busNotificacion.getSegundo();
        if (notificacion.getAccion() != null && notificacion.getAccion().getTipo().getId() != Tipo.NOMBRE.INVITACION.getId()){
            return new Par<>(HTTPCodes._400.getCodigo(), "Invitacion no valida");
        }

        // Obtenemos al usuario de la base de datos
        Par<Integer, Usuario> busUsuario = controladorUsuario.buscarUsuarioPorCorreo(invitado);
        int codResBusUsuario = busUsuario.getPrimero();
        if (codResBusUsuario > 0){
            return new Par<>(HTTPCodes._500.getCodigo(), "Ocurrio un error");
        }

        // Comprobamos que la notificacion sea para el usuario invitado
        Usuario usuarioInvitado = busUsuario.getSegundo();
        if (!notificacion.getReceptor().equals(usuarioInvitado)){
            return new Par<>(HTTPCodes._400.getCodigo(), "Ocurrio un error");
        }

        // Comprobamoos que la invitacion no halla sido rechazada
        if (notificacion.getAccion().isCompletada()){
            return new Par<>(HTTPCodes._400.getCodigo(), "Ya respondiste a la notificacion");
        }

        return new Par<>(-1, null);
    }

    private Par<Integer, String> realizarRechazoAGrupo(Claims token, JSONObject cuerpo){

        Par<Integer, String> resultadoComprobaciones = realizarComprobacionesPreviasRechazoGrupo(token, cuerpo);
        int codResComprobaciones = resultadoComprobaciones.getPrimero();

        // Algo salio mal
        if (codResComprobaciones != -1){
            return resultadoComprobaciones;
        }

        int idNotificacion = Integer.valueOf((String) cuerpo.get("idNotificacion"));
        ControladorNotificacion controladorNotificacion = new ControladorNotificacion();

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        Par<Integer, Notificacion> resBusNotificacion = controladorNotificacion.obtenerNotificacionConId(idNotificacion);
        Notificacion notificacion = resBusNotificacion.getSegundo();
        notificacion.getAccion().setCompletada(true);

        // Actualizamos el estado de la notificion
        int resActualizacion = controladorNotificacion.actualizarNotificacion(notificacion, entityManager);

        // Algo salio mal
        if (resActualizacion > 0){
            entityTransaction.rollback();
            entityManager.close();
            return new Par<>(HTTPCodes._500.getCodigo(), "Ocurrio un error");
        }

        // Se actualizo la notificacion exitosamente
        entityTransaction.commit();
        entityManager.close();

        return new Par<>(HTTPCodes._200.getCodigo(), "Se ha rechazado la invitacion");
    }

    private Runnable rechazarInvitacion(Context ctx){
        return new Runnable() {
            @Override
            public void run() {

                Claims token = (Claims) new Securata(ctx).validarYRetornarToken();

                if (token != null){

                    JSONObject respuesta = new JSONObject();

                    try{

                        JSONObject peticion = (JSONObject) new JSONParser().parse(ctx.body());

                        // La peticion no contiene los campos necesarios
                        if (!rechazoInvitacionContieneCamposNec(peticion)){
                            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Faltan campos por rellenar");
                            ctx.status(HTTPCodes._400.getCodigo());
                            ctx.result(respuesta.toJSONString());
                            return;
                        }

                        // Rechazamos la invitacion
                        Par<Integer, String> resultado = realizarRechazoAGrupo(token, peticion);
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, resultado.getSegundo());
                        ctx.status(resultado.getPrimero());
                        ctx.result(respuesta.toJSONString());
                        return;


                    }catch (Exception e){
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error");
                        ctx.status(HTTPCodes._500.getCodigo());
                        ctx.result(respuesta.toJSONString());
                        return;
                    }
                }
            }
        };
    }
    // ---------------------------------------
}
