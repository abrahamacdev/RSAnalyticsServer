package com.rsanalytics.controlador.seguridad;

import com.rsanalytics.controlador.managers.ControladorUsuario;
import io.javalin.http.Context;
import io.jsonwebtoken.Jwt;
import com.rsanalytics.modelo.pojo.rest.Usuario;
import org.json.simple.JSONObject;
import com.rsanalytics.utilidades.Constantes;
import com.rsanalytics.utilidades.rest.HTTPCodes;
import com.rsanalytics.utilidades.Par;
import com.rsanalytics.utilidades.rest.SecurityUtils;

import java.util.Map;

/**
 * El securata se encargara de proporcionar los tokens a los usuarios
 * que se loguen y permitira que se pueda acceder a recursos protegidos
 * del sistema
 */
public class Securata {

    private Context ctx;

    private ControladorUsuario controladorUsuario = new ControladorUsuario();
    private SecurityUtils securityUtils = new SecurityUtils();
    private TokensManejador tokensManejador = new TokensManejador();

    public Securata(Context ctx){
        this.ctx = ctx;
    }

    public void loguea(Usuario usuarioTemp){


        Par<Integer, Usuario> resultado = controladorUsuario.buscarUsuarioPorCorreo(usuarioTemp.getCorreo());
        int estado = resultado.getPrimero();

        JSONObject respuesta = new JSONObject();

        // Ocurrio un error en el servidor
        if (estado == 1){
            ctx.status(HTTPCodes._500.getCodigo());
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error en el servidor");
            ctx.result(respuesta.toJSONString());
            return;
        }

        // No existe usuario en la base de datos con ese correo
        else if (estado == 2){
            ctx.status(HTTPCodes._401.getCodigo());
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Compruebe que los datos introducidos son correctos");
            ctx.result(respuesta.toJSONString());
            return;
        }

        // El usuario existe en la base de datos
        Usuario usuarioReal = resultado.getSegundo();

        // Comprobamos que las contraseñas coincidan
        boolean contraseniasCoinciden = securityUtils.verificarContrasenia(usuarioTemp.getContrasenia(), usuarioReal.getContrasenia());

        // No coinciden
        if (!contraseniasCoinciden){
            ctx.status(HTTPCodes._401.getCodigo());
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Compruebe que los datos introducidos son correctos");
            ctx.result(respuesta.toJSONString());
            return;
        }

        // Creamos el token del usuario
        String tokenUsuario = tokensManejador.crearTokenParaUsuario(usuarioReal);

        // Ocurrio un erorr al crear el token
        if (tokenUsuario == null){
            ctx.status(HTTPCodes._500.getCodigo());
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Compruebe que los datos introducidos son correctos");
            ctx.result(respuesta.toJSONString());
            return;
        }

        // Le enviamos el token
        ctx.status(HTTPCodes._200.getCodigo());
        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.TOKEN.value, tokenUsuario);
        ctx.result(respuesta.toJSONString());
    }

    public Jwt validarYRetornarToken(){
        return validarYRetornarToken(this.ctx.headerMap());
    }

    public Jwt validarYRetornarToken(Map<String, String> header){

        JSONObject respuesta = null;

        // EL token no esta en la cabecera de la peticion
        if (!header.containsKey(Constantes.REST.PETICIONES_KEYS.AUTHORIZATION_HEADER.value)){
            respuesta = new JSONObject();
            ctx.status(HTTPCodes._401.getCodigo());
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "");
            ctx.result(respuesta.toJSONString());
            return null;
        }

        String token = header.get(Constantes.REST.PETICIONES_KEYS.AUTHORIZATION_HEADER.value);

        Par<Integer, Jwt> resultado = tokensManejador.comprobarValidezToken(token);

        int codigo = resultado.getPrimero();

        switch (codigo){

            // El token no es de fiar
            case 1:
                respuesta = new JSONObject();
                ctx.status(HTTPCodes._401.getCodigo());
                respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "No se ha podido verificar el token");
                ctx.result(respuesta.toJSONString());
                return null;

            // Ocurrio otro error desconocido
            case 2:
                respuesta = new JSONObject();
                ctx.status(HTTPCodes._500.getCodigo());
                respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error inesperado");
                ctx.result(respuesta.toJSONString());
                return null;

            // El token ha caducado
            case 3:
                respuesta = new JSONObject();
                ctx.status(HTTPCodes._470.getCodigo());
                respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "La sesion ha caducado");
                ctx.result(respuesta.toJSONString());
                return null;

        }

        // Retornamos el token
        return resultado.getSegundo();
    }
}
