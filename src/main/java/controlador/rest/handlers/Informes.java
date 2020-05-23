package controlador.rest.handlers;

import controlador.informes.GestorInforme;
import controlador.managers.ControladorUsuario;
import controlador.managers.informes.ControladorInforme;
import controlador.managers.informes.ControladorInformeInmueble;
import controlador.seguridad.Securata;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import modelo.pojo.Municipio;
import modelo.pojo.rest.Usuario;
import modelo.pojo.scrapers.Informe;
import modelo.pojo.scrapers.Inmueble;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.tinylog.Logger;
import utilidades.Constantes;
import utilidades.Par;
import utilidades.Utils;
import utilidades.inmuebles.InmuebleUtils;
import utilidades.rest.HTTPCodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.List;
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

        // Lista de informes
        app.get(Constantes.REST.INFORME.INFORME_PATH.value + Constantes.REST.INFORME.LISTAR_ENDPOINT.value,
                (ctx) -> ejecutar(this::listarInformes, ctx));

        // Descargar informe
        app.post(Constantes.REST.INFORME.INFORME_PATH.value + Constantes.REST.INFORME.DESCARGAR_ENDPOINT.value,
                (ctx) -> ejecutar(this::descargarInforme, ctx));


    }

    // ----- Solicitud informe -----
    private boolean comprobarCamposSolicitudInforme(JSONObject root){

        int suma = 0;

        // Ciudad sobre la que se realizara el informe
        suma += root.containsKey("municipio") ? 1 : 0;

        // Tipo de contrato sobre el que se reealizara el informe
        suma += root.containsKey("idTipoContrato") ? 1 : 0;

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


    // ----- Listar informes del usuario -----
    private void obtenerListadoInformes(Claims token, Context ctx){

        JSONObject respuesta = new JSONObject();

        // Buscamos al usuario en la base de datos
        ControladorUsuario controladorUsuario = new ControladorUsuario();
        Par<Integer, Usuario> resBusUs = controladorUsuario.buscarUsuarioPorCorreo(token.getSubject());
        int codResBusUs = resBusUs.getPrimero();
        if (codResBusUs != 0){
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrió un error");
            ctx.status(HTTPCodes._500.getCodigo());
            ctx.result(respuesta.toString());
            return;
        }

        // Obtenemos los informes del usuario
        ControladorInforme controladorInforme = new ControladorInforme();
        Par<Exception, List<Informe>> resBusInfs = controladorInforme.obtenerInformesDelUsuario(resBusUs.getSegundo());
        if (!resBusInfs.primeroEsNulo()){
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrió un error");
            ctx.status(HTTPCodes._500.getCodigo());
            ctx.result(respuesta.toString());
            return;
        }

        // Comprobamos que halla alguno en la lista antes de continuar
        List<Informe> informes = resBusInfs.getSegundo();
        JSONArray jsonArrayInformes = new JSONArray();
        if (informes.size() == 0){
            respuesta.put("informes", jsonArrayInformes);
            ctx.status(HTTPCodes._200.getCodigo());
            ctx.result(respuesta.toString());
            return;
        }


        ControladorInformeInmueble controladorInformeInmueble = new ControladorInformeInmueble();
        for (Informe informe : resBusInfs.getSegundo()){

            // Obtenemos el primer inmueble de cada informe para saber el municipio sobre el que se realizó el informe
            Par<Exception, Inmueble> resBusPrimerInmueble = controladorInformeInmueble.obtenerPrimerInmuebleDelInforme(informe);

            if (!resBusPrimerInmueble.segundoEsNulo()){
                Municipio municipio = resBusPrimerInmueble.getSegundo().getMunicipio();

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(informe.getFechaCreacionSolicitud());
                int dia = calendar.get(Calendar.DAY_OF_MONTH);
                String mesEnEspaniol = Utils.mes2Texto(calendar.get(Calendar.MONTH));
                int anio = calendar.get(Calendar.YEAR);
                String hora = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
                String minutos = String.format("%02d", calendar.get(Calendar.MINUTE));

                JSONObject temp = new JSONObject();
                temp.put("id", informe.getId());
                temp.put("municipio", municipio.getNombre());
                temp.put("fechaSolicitud", dia + " de " + mesEnEspaniol + " de " + anio + " a las " + hora + ":" + minutos);
                temp.put("pendiente", informe.getFechaRealizacion() > 0 ? false : true);

                if (informe.getRutaArchivo() != null){
                    File nombreArchivo = new File(informe.getRutaArchivo());
                    temp.put("nombreArchivo", nombreArchivo.getName());
                }

                jsonArrayInformes.add(temp);
            }
        }

        respuesta.put("informes", jsonArrayInformes);
        ctx.status(HTTPCodes._200.getCodigo());
        ctx.result(respuesta.toString());
        return;
    }

    private Runnable listarInformes(Context ctx){
        return new Runnable() {
            @Override
            public void run() {

                Jwt token = new Securata(ctx).validarYRetornarToken();

                if (token != null){
                    obtenerListadoInformes((Claims) token.getBody(), ctx);
                }

            }
        };
    }
    // ------------------------------


    // ----- Descargar informe solicitado -----
    private boolean comprobarCamposDescargaInforme(JSONObject cuerpo){
        return cuerpo.containsKey("idInforme");
    }

    private void comenzarDescargaInforme(Context ctx, Claims token, JSONObject cuerpo){

        int idInforme = ((Long) cuerpo.get("idInforme")).intValue();

        // Buuscamos el usuario en la base de datos
        ControladorUsuario controladorUsuario = new ControladorUsuario();
        Par<Integer, Usuario> resBusUsu = controladorUsuario.buscarUsuarioPorCorreo(token.getSubject());
        if (resBusUsu.getPrimero() > 0){
            ctx.status(HTTPCodes._500.getCodigo());
            return;
        }

        // Buscamos el informe en la base de datos
        ControladorInforme controladorInforme = new ControladorInforme();
        Par<Exception, Informe> resBusInf = controladorInforme.obtenerInformeConId(idInforme);
        if (!resBusInf.primeroEsNulo()){
            ctx.status(HTTPCodes._500.getCodigo());
            return;
        }

        // Comprobamos que el informe pertenezca al usuario
        if (!resBusInf.getSegundo().getUsuario().equals(resBusUsu.getSegundo())){
            ctx.status(HTTPCodes._500.getCodigo());
            return;
        }

        File informe = new File(resBusInf.getSegundo().getRutaArchivo());
        try {
            ctx.status(HTTPCodes._200.getCodigo());
            ctx.result(new FileInputStream(informe));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ctx.status(HTTPCodes._500.getCodigo());
        }
    }

    private Runnable descargarInforme(Context ctx){
        return new Runnable() {
            @Override
            public void run() {

                Jwt token = new Securata(ctx).validarYRetornarToken();

                if (token != null){

                    JSONObject respuesta = new JSONObject();

                    try {
                        JSONObject cuerpo = (JSONObject) new JSONParser().parse(ctx.body());

                        // Comprobamos que la peticion contenga los campos necesarios
                        if (!comprobarCamposDescargaInforme(cuerpo)){
                            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Faltan campos por rellenar");
                            ctx.status(HTTPCodes._400.getCodigo());
                            ctx.result(respuesta.toJSONString());
                            return;
                        }

                        // Comenzamos la descarga del informe
                        comenzarDescargaInforme(ctx, (Claims) token.getBody(),cuerpo);

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
