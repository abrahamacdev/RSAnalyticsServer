package controlador.rest.handlers;

import controlador.managers.ControladorGrupo;
import controlador.managers.ControladorUsuario;
import controlador.seguridad.Securata;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import modelo.pojo.rest.Usuario;
import org.apache.http.protocol.HTTP;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.tinylog.Logger;
import utilidades.rest.HTTPCodes;
import utilidades.Constantes;
import utilidades.Par;
import utilidades.Utils;

import java.util.concurrent.ExecutorService;

public class Usuarios extends AbstractHandler{


    public Usuarios(Javalin app, ExecutorService piscina){
        super(app,piscina);
    }

    @Override
    protected void registrarHandlers(){

        // Login de un usuario
        app.post(Constantes.REST.USUARIO.USUARIO_PATH.value + Constantes.REST.USUARIO.LOGIN_ENDPOINT.value,
                (ctx) -> super.ejecutar(this::login, ctx));

        // Registro de un nuevo usuario
        app.post(Constantes.REST.USUARIO.USUARIO_PATH.value + Constantes.REST.USUARIO.REGISTRO_ENDPOINT.value,
                (ctx) -> super.ejecutar(this::registrar, ctx));

        // Información general de un usuario
        app.get(Constantes.REST.USUARIO.USUARIO_PATH.value + Constantes.REST.USUARIO.INFORMACION_GENERAL_ENDPOINT.value,
                (ctx) -> super.ejecutar(this::informacionGeneral, ctx));
    }


    // ----- Información General -----
    private void obtenerInformacionGeneral(Context ctx, Claims body) {

        JSONObject respuesta = new JSONObject();

        ControladorUsuario controladorUsuario = new ControladorUsuario();
        Par<Integer, Usuario> resBusUs = controladorUsuario.buscarUsuarioPorCorreo(body.getSubject());
        int codResBusUs = resBusUs.getPrimero();
        if (codResBusUs != 0){
            ctx.status(HTTPCodes._500.getCodigo());
            respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error");
            ctx.result(respuesta.toJSONString());
            return;
        }

        ControladorGrupo controladorGrupo = new ControladorGrupo();
        int resEsResponsable = controladorGrupo.elUsuarioEsResponsableDeGrupo(body.getSubject());

        Usuario usuario = resBusUs.getSegundo();

        respuesta.put("nombre", usuario.getNombre());
        respuesta.put("primerApellido", usuario.getPrimerApellido());
        respuesta.put("correo", usuario.getCorreo());
        respuesta.put("genero", usuario.getGenero());
        respuesta.put("esResponsable", resEsResponsable == 0 || resEsResponsable == 2);

        ctx.status(HTTPCodes._200.getCodigo());
        ctx.result(respuesta.toJSONString());

    }

    private Runnable informacionGeneral(Context ctx){
        return new Runnable() {
            @Override
            public void run() {

                Jwt token = (Jwt) new Securata(ctx).validarYRetornarToken();

                if (token != null){
                    obtenerInformacionGeneral(ctx, (Claims) token.getBody());
                }
            }
        };
    }
    // -------------------------------


    // ----- Login -----
    private Runnable login(Context ctx){

        Runnable tarea = new Runnable() {
            @Override
            public void run() {

                try {

                    JSONObject cuerpo  = (JSONObject) new JSONParser().parse(ctx.body());

                    // Creamos un objeto usuario temporal
                    Usuario usuarioTemp = Usuario.fromJson(cuerpo);

                    // Comprobamos que contennga los campos necesarios para hacer el login
                    if (!contieneCamposLogin(usuarioTemp)){
                        JSONObject respuesta = new JSONObject();
                        ctx.status(HTTPCodes._400.getCodigo());
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Faltan campos por rellenar");
                        ctx.json(respuesta);
                        return;
                    }

                    // Dejamos que el securata se encargue del resto
                    new Securata(ctx)
                        .loguea(usuarioTemp);


                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        return tarea;
    }

    private boolean contieneCamposLogin(Usuario usuario){
        return usuario.getContrasenia() != null && usuario.getCorreo() != null;
    }
    // -----------------


    // ----- Registro -----
    private Runnable registrar(Context ctx){

        Runnable tarea = new Runnable() {

            @Override
            public void run() {

                JSONObject respuesta = new JSONObject();

                try {

                    JSONObject cuerpo = (JSONObject) new JSONParser().parse(ctx.body());

                    // Creamoos un objeto usuario a partir de los datos de la peticion
                    Usuario usuarioTemp = Usuario.fromJson(cuerpo);

                    // Comprobamos que se tengan todos los campos necesarios para el registro
                    if (!contieneCamposRegistro(usuarioTemp)){
                        ctx.status(HTTPCodes._400.getCodigo());
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Faltan campos por rellenar");
                        ctx.json(respuesta.toJSONString());
                        return;
                    }

                    // COmprobamos que todos los campos sean validos
                    Par<Boolean, String> camposValidados = camposRegistroValidos(usuarioTemp);

                    // Alguno de los campos no es valido
                    if (!camposValidados.getPrimero()){
                        ctx.status(HTTPCodes._400.getCodigo());
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, camposValidados.getSegundo());
                        ctx.json(respuesta);
                        return;
                    }

                    // Comprobamos que no exista un usuario con el mismo correo
                    ControladorUsuario controladorUsuario = new ControladorUsuario();

                    Par<Integer, Usuario> resultado = controladorUsuario.buscarUsuarioPorCorreo(usuarioTemp.getCorreo());
                    int estado = resultado.getPrimero();

                    // Existe un usuario con esas credenciales
                    if (estado == 0){
                        ctx.status(HTTPCodes._400.getCodigo());
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ya existe un usuario con ese correo");
                        ctx.json(respuesta);
                        return;
                    }

                    // Ha ocurrido un error
                    else if (estado == 1){
                        ctx.status(HTTPCodes._500.getCodigo());
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error en el servidor");
                        ctx.json(respuesta);
                        return;
                    }

                    // Insertamos al usuario en la base de datos
                    Par<Integer, Usuario> respuestaCreacionUsuario = controladorUsuario.guardarNuevoUsuario(usuarioTemp);

                    // Ocurrio un error al insertar el registro
                    if (respuestaCreacionUsuario.getPrimero() == 1){
                        ctx.status(HTTPCodes._500.getCodigo());
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error en el servidor");
                        ctx.json(respuesta);
                        return;
                    }

                    // Se ha creado el registro exitosamente
                    else {
                        ctx.status(HTTPCodes._201.getCodigo());
                        respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Se ha registrado exitosamente");
                        ctx.json(respuesta);
                        return;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    ctx.status(HTTPCodes._500.getCodigo());
                    respuesta.put(Constantes.REST.RESPUESTAS_KEYS.MSG.value, "Ocurrio un error en el servidor");
                    ctx.json(respuesta.toJSONString());
                    Logger.error("Ocurrio un error inesperado", e);
                    return;
                }
            }
        };

        return tarea;
    }

    /**
     * Comprobamos que la peticion contenga todos los datos necesarioos para dar de alta a un
     * usuario
     * @param usuario
     * @return
     */
    private boolean contieneCamposRegistro(Usuario usuario){

        boolean contieneNombre =  usuario.getNombre() != null;
        boolean contienePrimerAp = usuario.getPrimerApellido() != null;
        boolean contieneSegundoAp = usuario.getSegundoApellido() != null;
        boolean contieneTelefono = usuario.getSegundoApellido() != null;
        boolean contieneCorreo = usuario.getSegundoApellido() != null;
        boolean contieneGenero = usuario.getGenero() != null;
        boolean contieneContrasenia = usuario.getSegundoApellido() != null;

        return contieneNombre && contienePrimerAp && contieneSegundoAp && contieneGenero && contieneTelefono && contieneCorreo && contieneContrasenia;
    }

    /**
     * Comprobamos que todos los campos necesarios para el registro sean validos
     * @param usuario
     * @return
     */
    private Par<Boolean, String> camposRegistroValidos(Usuario usuario){

        Par<Boolean, String> par = new Par<>(true, null);

        // NOmbre no valido
        if (!Utils.nombreValido(usuario.getNombre())){
            par = new Par<>(false, "Nombre no valido");
            return par;
        }

        // Primer apellido no valido
        if (!Utils.nombreValido(usuario.getPrimerApellido())){
            par = new Par<>(false, "Primer apellido no valido");
            return par;
        }

        // Segundo apellido no valido
        if (!Utils.nombreValido(usuario.getSegundoApellido())){
            par = new Par<>(false, "Segundo apellido no valido");
            return par;
        }

        // Telefono no valido
        if (!Utils.telefonoValido(usuario.getTelefono())){
            par = new Par<>(false, "Telefono no valido");
            return par;
        }

        // Correo no valido
        if (!Utils.correoValido(usuario.getCorreo())){
            par = new Par<>(false, "Correo no valido");
            return par;
        }

        // Contrasenia no valida
        if (!Utils.contraseniaValida(usuario.getContrasenia())){
            par = new Par<>(false, "Contrasenia no valida");
        }

        return par;
    }
    // --------------------
}
