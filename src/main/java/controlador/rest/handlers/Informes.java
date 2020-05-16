package controlador.rest.handlers;

import controlador.informes.GestorInforme;
import controlador.managers.ControladorUsuario;
import controlador.seguridad.Securata;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import modelo.pojo.rest.Usuario;
import modelo.pojo.scrapers.Informe;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.tinylog.Logger;
import utilidades.Constantes;
import utilidades.Par;
import utilidades.inmuebles.InmuebleUtils;
import utilidades.rest.HTTPCodes;

import java.util.concurrent.ExecutorService;

public class Informes extends AbstractHandler{

    public Informes(Javalin app, ExecutorService piscina) {
        super(app, piscina);
    }

    @Override
    void registrarHandlers() {

        // Solicitud de creacion de informes
        app.post(Constantes.REST.INFORME.INFORME_PATH.value + Constantes.REST.INFORME.SOLICITAR_ENDPOINT.value,
                (ctx) -> ejecutar(this::solicitarInforme, ctx));
    }

    // ----- Solicitud informe -----
    private boolean comprobarCamposSolicitudInforme(JSONObject root){

        int suma = 0;

        // Ciudad sobre la que se realizara el informe
        suma += root.containsKey("municipio") ? 1 : 0;

        // Tipo de contrato sobre el que se reealizara el informe
        suma += root.containsKey("tipoContrato") ? 1 : 0;

        // TIpo de inmueble sobre el que se realizara el informe
        Integer idTipoInmueble = root.containsKey("idTipoInmueble") ? ((Long) root.get("idTipoInmueble")).intValue() : null;
        suma += InmuebleUtils.idTipoInmuebleValido(idTipoInmueble) ? 1 : 0;

        return suma == 3;
    }

    private void crearSolicitudInforme(Context ctx, Claims token, JSONObject cuerpo) {

        JSONObject respuesta = new JSONObject();

        // Obbtenemos el usuario que esta realizando la peticion
        GestorInforme gestorInforme = new GestorInforme();
        ControladorUsuario controladorUsuario = new ControladorUsuario();
        Par<Integer, Usuario> resBusUs = controladorUsuario.buscarUsuarioPorCorreo(token.getSubject());
        int codResBusUs = resBusUs.getPrimero();
        if (codResBusUs > 0){
            ctx.status(HTTPCodes._500.getCodigo());
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error");
            ctx.result(respuesta.toJSONString());
            return;
        }

        // Creamos la solicitud y la gguardamos en la base de datos
        Par<Exception, String> resCreacionSolicitud = gestorInforme.crearSolicitud(cuerpo, resBusUs.getSegundo());
        if (resCreacionSolicitud.getPrimero() != null){
            ctx.status(HTTPCodes._500.getCodigo());
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error");
            ctx.result(respuesta.toJSONString());
            return;
        }

        // Toodo salio bien
        ctx.status(HTTPCodes._200.getCodigo());
        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, resCreacionSolicitud.getSegundo());
        ctx.result(respuesta.toJSONString());
    }

    private Runnable solicitarInforme(Context ctx){
        return new Runnable() {
            @Override
            public void run() {

                Jwt token = new Securata(ctx).validarYRetornarToken();

                if (token != null){

                    JSONObject respuesta = new JSONObject();

                    try {
                        JSONObject cuerpo = (JSONObject) new JSONParser().parse(ctx.body());

                        // Comprobamos que la peticion contenga los campos necesarios
                        if (!comprobarCamposSolicitudInforme(cuerpo)){
                            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Faltan campos por rellenar");
                            ctx.status(HTTPCodes._400.getCodigo());
                            ctx.result(respuesta.toJSONString());
                            return;
                        }

                        // Creamos la solicitud del informe
                        crearSolicitudInforme(ctx, (Claims) token.getBody(), cuerpo);

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
    // ------------------------------
}
