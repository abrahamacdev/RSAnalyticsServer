package controlador.managers;

import modelo.pojo.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.tinylog.Logger;
import utilidades.HTTPCodes;
import utilidades.Par;
import utilidades.Utils;

import javax.persistence.Query;
import java.util.List;

public class UsuarioManager {

    // ----- Read -----

    /**
     * Buscaremos al usuario con el correo proporcionado
     * @param correo
     * @return Usuario,null -> En caso de que exista |
     *         null, Exception -> En caso de error |
     *         null,null -> En caso de que no exista
     */
    public Par<Usuario, Exception> buscarUsuarioPorCorreo(String correo){

        Session session = Utils.crearNuevaSesion();
        Transaction transaction = null;
        Par<Usuario, Exception> respuesta;

        try {

            transaction = session.beginTransaction();

            Query query = session.createQuery("FROM Usuario AS us WHERE us.correo = :correo");
            query.setParameter("correo", correo);

            List<Usuario> listaUsuarios = query.getResultList();

            transaction.commit();

            // No existe ningun usuario con ese correo
            if (listaUsuarios.size() == 0){
                respuesta = new Par<>(null, null);
            }

            // Exite un usuario con ese correo
            else {
                respuesta = new Par<>(listaUsuarios.get(0), null);
            }

        }catch (Exception e){
            e.printStackTrace();
            respuesta = new Par<>(null, e);
        }finally {
            session.close();
        }

        return respuesta;
    }
    // ----------------


    // ----- Create -----

    /**
     * Creamos un nuevo reegistro en la base de datos a partir del objeto usuario recibido
     * @param usuario
     * @return 1,null -> En caso de que halla ocurrido un error
     *         0,Usuario -> En caso de que se halla podido crear el registro
     */
    public Par<Integer, Usuario> crearNuevoUsuario(Usuario usuario){

        Session session = Utils.crearNuevaSesion();
        Transaction transaction = null;
        Par<Integer, Usuario> respuesta = null;

        try {

            transaction = session.beginTransaction();

            // Ciframos la contraseña antes de guardarla en el servidor
            Par<byte[], byte[]> contraseniaConSalt = Utils.cifrarContrasenia(usuario.getContrasenia());
            usuario.setContrasenia(contraseniaConSalt.getPrimero());
            usuario.setSalt(contraseniaConSalt.getSegundo());

            Usuario usuarioCreado = (Usuario) session.save(usuario);

            transaction.commit();

            respuesta = new Par<>(0, usuarioCreado);

        }catch (Exception e){
            Logger.error("Ocurrio un error al insertar un nuevo usuario", e);
            respuesta = new Par<>(1, null);
        }finally {
            session.close();
        }

        return respuesta;

    }
    // ------------------

}
