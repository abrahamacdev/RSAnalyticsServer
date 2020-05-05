package controlador.rest.handlers;

import controlador.managers.ControladorNotificacion;
import controlador.seguridad.Securata;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import modelo.pojo.rest.Accion;
import modelo.pojo.rest.Notificacion;
import modelo.pojo.rest.Tipo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utilidades.Constantes;
import utilidades.rest.HTTPCodes;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.ToIntFunction;

public class Notificaciones extends AbstractHandler {

    public Notificaciones(Javalin app, ExecutorService piscina) {
        super(app, piscina);
    }

    @Override
    void registrarHandlers() {

        // Listado de notificaciones
        app.get(Constantes.REST.USUARIO.USUARIO_PATH.value + Constantes.REST.USUARIO.NOTIFICACIONES_PATH.value,
                (ctx) -> ejecutar(this::listarNotificaciones, ctx));

        // Comprobacion existencia notificaciones no leidas
        app.get(Constantes.REST.USUARIO.USUARIO_PATH.value + Constantes.REST.USUARIO.NOTIFICACIONES_PATH.value + Constantes.REST.USUARIO.NOTIFICACIONES_PENDIENTES_ENDPOINT.value,
                (ctx) -> ejecutar(this::comprobarNotificacionesNoLeidas, ctx));

        // Marcar notificaciones leidas
        app.post(Constantes.REST.USUARIO.USUARIO_PATH.value + Constantes.REST.USUARIO.NOTIFICACIONES_PATH.value + Constantes.REST.USUARIO.MARCAR_NOTIFICACIONES_LEIDAS.value,
                (ctx) -> ejecutar(this::marcarNotificacionesLeidas, ctx));
    }


    // ----- Listado de notificaciones -----
    private JSONObject parsearAccionDeNotificion2Json(Notificacion notificacion){

        JSONObject accionParseada = new JSONObject();
        Accion accion = notificacion.getAccion();
        int idTipoAccion = accion.getTipo().getId();

        // Invitacion a grupo
        if (idTipoAccion == Tipo.NOMBRE.INVITACION.getId()){
            accionParseada.put("tipo", Tipo.NOMBRE.INVITACION.getId());
            accionParseada.put("nombreGrupo", accion.getGrupo().getNombre());
        }

        accionParseada.put("completada", accion.isCompletada());

        return accionParseada;
    }

    private void realizarListadoNotificaciones(Context ctx, Claims token){

        JSONObject respuesta = new JSONObject();

        String correoUsuario = token.getSubject();

        ControladorNotificacion controladorNotificacion = new ControladorNotificacion();
        Par<Integer, List<Notificacion>> resBusNotificaciones = controladorNotificacion.obtenerTodasNotificaciones(correoUsuario);
        int codResBusNotificaciones = resBusNotificaciones.getPrimero();
        switch (codResBusNotificaciones){

            // No tiene notificaciones
            case 1:
                respuesta.put(Constantes.REST.RESPUESTAS_KEYS.NOTIFICACIONES.value, new JSONArray().toJSONString());
                ctx.status(HTTPCodes._200.getCodigo());
                ctx.result(respuesta.toJSONString());
                return;

            // Ocurrio un error
            case 2:
                respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error");
                ctx.status(HTTPCodes._500.getCodigo());
                ctx.result(respuesta.toJSONString());
                return;
        }

        List<Notificacion> notificaciones = resBusNotificaciones.getSegundo();
        JSONArray notificacionesParseadas = new JSONArray();

        // Convertimos cada objeto Notificacion a un JSONObject
        notificaciones.stream()
                .map(notificacion -> {
                    JSONObject notificacionParseada = new JSONObject();
                    notificacionParseada.put("id", notificacion.getId());
                    notificacionParseada.put("fecha", notificacion.fechaEnvio2Millis());
                    notificacionParseada.put("mensaje", notificacion.getMensaje());
                    notificacionParseada.put("emisor", notificacion.getEmisor().getNombre());
                    notificacionParseada.put("leida", notificacion.isLeida());

                    // Si la notificacion tiene una accion la añadiremos
                    if (notificacion.getAccion() != null){
                        notificacionParseada.put("accion", parsearAccionDeNotificion2Json(notificacion));
                    }

                    return notificacionParseada;
                })
                .forEach(notificacionParseada -> {
                    notificacionesParseadas.add(notificacionParseada);
                });



        // Devolvemos el array con las notificaciones
        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.NOTIFICACIONES.value, notificacionesParseadas);
        ctx.status(HTTPCodes._200.getCodigo());
        ctx.result(respuesta.toJSONString());
        return;
    }

    private Runnable listarNotificaciones(Context ctx){
        return new Runnable() {
            @Override
            public void run() {

                Jwt token = (Jwt) new Securata(ctx).validarYRetornarToken();

                if (token != null){
                    realizarListadoNotificaciones(ctx, (Claims) token.getBody());
                }
            }
        };
    }
    // -------------------------------------


    // ----- Comprobacion notificaciones no leidas -----
    private Runnable comprobarNotificacionesNoLeidas(Context ctx){
        return new Runnable() {
            @Override
            public void run() {

                Jwt token = new Securata(ctx).validarYRetornarToken();

                if (token != null){

                    JSONObject respuesta = new JSONObject();

                    ControladorNotificacion controladorNotificacion = new ControladorNotificacion();
                    String correo = ((Claims) token.getBody()).getSubject();

                    // Comprobamos si tiene notificaciones no leidas
                    int notificacionesNoLeidas = controladorNotificacion.tieneNotificacionesNoLeidas(correo);
                    if (notificacionesNoLeidas == 2){
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error");
                        ctx.status(HTTPCodes._500.getCodigo());
                        ctx.result(respuesta.toJSONString());
                        return;
                    }

                    respuesta.put(Constantes.REST.RESPUESTAS_KEYS.HAY_NOTIFICACIONES_PENDIENTES.value, notificacionesNoLeidas==0);
                    ctx.status(HTTPCodes._200.getCodigo());
                    ctx.result(respuesta.toJSONString());
                    return;

                }
            }
        };
    }
    // -------------------------------------------------


    // ----- Marcar notificaciones leidas -----
    private boolean comprobarCamposMarcarNotiLeidas(JSONObject cuerpo){

        if (!cuerpo.containsKey("idsNotificaciones")){
            return false;
        }

        return true;
    }

    private void realizarMarcadoNotificacionesLeidas(Context ctx, Claims token, JSONObject cuerpo){

        JSONObject respuesta = new JSONObject();
        String correo = token.getSubject();

        System.out.println(cuerpo.get("idsNotificaciones"));

        // Obtenemos cada id del JSONArray y lo metemos a la lista de ids
        List<Integer> idsNotificaciones = ((JSONArray) cuerpo.get("idsNotificaciones")).stream()
                .mapToInt(new ToIntFunction(){
                    @Override
                    public int applyAsInt(Object value) {
                        return ((Long)value).intValue();
                    }
                })
                .collect(ArrayList<Integer>::new, ArrayList::add, ArrayList::addAll);

        // Obtenemos todas las notificaciones con el id deseado
        ControladorNotificacion controladorNotificacion = new ControladorNotificacion();
        Par<Integer, List<Notificacion>> resBusquedaNotificaciones = controladorNotificacion.obtenerNotificacionesConIdDelUsuario(idsNotificaciones, correo);
        int codResBusquedaNotificaciones = resBusquedaNotificaciones.getPrimero();
        if (codResBusquedaNotificaciones == 2){
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error");
            ctx.status(HTTPCodes._500.getCodigo());
            ctx.result(respuesta.toJSONString());
            return;
        }

        List<Notificacion> notificacionesNoLeidas = resBusquedaNotificaciones.getSegundo();

        // Comprobamos que las notificaciones ha marcar existan todas
        if (notificacionesNoLeidas.size() != idsNotificaciones.size()){
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error");
            ctx.status(HTTPCodes._500.getCodigo());
            ctx.result(respuesta.toJSONString());
            return;
        }

        notificacionesNoLeidas.forEach((notificacion -> notificacion.setLeida(true)));

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        notificacionesNoLeidas.forEach((notificacion) -> {
            int res = controladorNotificacion.actualizarNotificacion(notificacion);

            // NO se pudo realizar la actualizacion
            if (res == 1){
                entityTransaction.rollback();
                entityManager.close();

                respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error");
                ctx.status(HTTPCodes._500.getCodigo());
                ctx.result(respuesta.toJSONString());
                return;
            }
        });

        entityTransaction.commit();
        entityManager.close();

        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Notificaciones actualizadas");
        ctx.status(HTTPCodes._200.getCodigo());
        ctx.result(respuesta.toJSONString());
    }

    private Runnable marcarNotificacionesLeidas(Context ctx){
        return new Runnable() {
            @Override
            public void run() {

                Jwt token = new Securata(ctx).validarYRetornarToken();

                if (token != null){

                    JSONObject respuesta = new JSONObject();

                    try {
                        JSONObject cuerpo = (JSONObject) new JSONParser().parse(ctx.body());

                        // Comprobamos que la peticion contenga los campos necesarios
                        if (!comprobarCamposMarcarNotiLeidas(cuerpo)){
                            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Faltan campos por rellenar");
                            ctx.status(HTTPCodes._400.getCodigo());
                            ctx.result(respuesta.toJSONString());
                            return;
                        }

                        // Intentamos marcas las notificaciones como leidas
                        realizarMarcadoNotificacionesLeidas(ctx, (Claims) token.getBody(), cuerpo);

                    } catch (ParseException e) {
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error");
                        ctx.status(HTTPCodes._500.getCodigo());
                        ctx.result(respuesta.toJSONString());
                        return;
                    }
                }
            }
        };
    }
    // ----------------------------------------
}
