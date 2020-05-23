package controlador.rest.handlers;

import controlador.managers.ControladorMunicipio;
import controlador.seguridad.Securata;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import modelo.pojo.Municipio;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utilidades.Constantes;
import utilidades.Par;
import utilidades.rest.HTTPCodes;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class Municipios extends AbstractHandler{

    public Municipios(Javalin app, ExecutorService piscina) {
        super(app, piscina);
    }

    @Override
    void registrarHandlers() {

        // Comprobamos los municipios que tienen un nombre parecido al enviado
        app.post(Constantes.REST.MUNICIPIO.MUNICIPIOS_PATH.value + Constantes.REST.MUNICIPIO.BUSCAR_PARECIDO_ENDPOINT.value,
                (ctx) -> ejecutar(this::buscarMunicipiosParecidos, ctx));

    }

    // ----- Comprobamos los municipios cuyo nombre se pareceal enviado -----
    private boolean comprobarCamposMunicipiosParecidos(JSONObject jsonObject){
        return jsonObject.containsKey("palabraClave");
    }

    private void realizarBusquedaMunicipiosParecidos(Context ctx, JSONObject jsonObject){

        JSONObject respuesta = new JSONObject();

        ControladorMunicipio controladorMunicipio = new ControladorMunicipio();
        Par<Exception, List<Municipio>> resBusParsMun = controladorMunicipio.buscarMunicipiosConNombreParecidoA((String) jsonObject.get("palabraClave"));
        if (!resBusParsMun.primeroEsNulo()){
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error");
            ctx.status(HTTPCodes._500.getCodigo());
            ctx.result(respuesta.toJSONString());
            return;
        }

        JSONArray parecidos = new JSONArray();
        List<Municipio> municipios = resBusParsMun.getSegundo();
        for (Municipio municipio : municipios){
            parecidos.add(municipio.getNombre());
        }

        respuesta.put("municipios", parecidos);
        ctx.status(HTTPCodes._200.getCodigo());
        ctx.result(respuesta.toJSONString());
        return;
    }

    private Runnable buscarMunicipiosParecidos(Context ctx){
        return new Runnable() {
            @Override
            public void run() {

                Jwt token = new Securata(ctx).validarYRetornarToken();

                if (token != null){

                    JSONObject respuesta = new JSONObject();

                    try {
                        JSONObject cuerpo = (JSONObject) new JSONParser().parse(ctx.body());

                        // Comprobamos que la peticion contenga los campos necesarios
                        if (!comprobarCamposMunicipiosParecidos(cuerpo)){
                            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Faltan campos por rellenar");
                            ctx.status(HTTPCodes._400.getCodigo());
                            ctx.result(respuesta.toJSONString());
                            return;
                        }

                        // Comenzamos la descarga del informe
                        realizarBusquedaMunicipiosParecidos(ctx, cuerpo);

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
    // --------------------------------
}
