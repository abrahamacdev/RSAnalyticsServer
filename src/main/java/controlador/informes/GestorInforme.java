package controlador.informes;

import controlador.managers.informes.ControladorInforme;
import controlador.managers.ControladorMunicipio;
import controlador.managers.informes.ControladorInformeInmueble;
import controlador.managers.inmuebles.ControladorInmueble;
import controlador.managers.inmuebles.ControladorTipoContrato;
import modelo.pojo.Municipio;
import modelo.pojo.rest.Usuario;
import modelo.pojo.scrapers.Informe;
import modelo.pojo.scrapers.Inmueble;
import modelo.pojo.scrapers.TipoContrato;
import modelo.pojo.scrapers.informe_inmueble.InformeInmueble;
import org.json.simple.JSONObject;
import org.tinylog.Logger;
import utilidades.Constantes;
import utilidades.Par;
import utilidades.Utils;
import utilidades.inmuebles.InmuebleUtils;
import utilidades.inmuebles.TipoInmueble;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class GestorInforme {

    public GestorInforme(){}

    public Par<Exception, String>  crearSolicitud(JSONObject datos, Usuario usuario){

        ControladorMunicipio controladorMunicipio = new ControladorMunicipio();
        ControladorInmueble controladorInmueble = new ControladorInmueble();
        ControladorTipoContrato controladorTipoContrato = new ControladorTipoContrato();

        String municipio = (String) datos.get("municipio");
        Par<Exception, Municipio> resBusMunicipio = controladorMunicipio.buscarMunicipioPorNombre(municipio);
        if (resBusMunicipio.getPrimero() != null){
            return new Par<>(resBusMunicipio.getPrimero(), null);
        }

        int idTipoContrato = ((Long) datos.get("idTipoContrato")).intValue();
        Par<Exception, TipoContrato> resBusTipCon = controladorTipoContrato.buscarTipoContratoConId(idTipoContrato);
        if (resBusTipCon.getPrimero() != null){
            return new Par<>(resBusTipCon.getPrimero(), null);
        }

        Integer idTipoInmueble = datos.containsKey("idTipoInmueble") ? ((Long) datos.get("idTipoInmueble")).intValue() : null;
        Integer idSubtipoInmueble = null;
        if (InmuebleUtils.idTipoInmuebleEs(idTipoInmueble, TipoInmueble.VIVIENDA)){
            idSubtipoInmueble = datos.containsKey("idSubtipoInmueble") ? ((Long) datos.get("idSubtipoInmueble")).intValue() : null;
        }

        // Añadimos unos parametros a la busqueda
        HashMap<Integer, Object> paramsAtributos = new HashMap();
        paramsAtributos.put(68, idTipoInmueble);
        if (idSubtipoInmueble != null){
            paramsAtributos.put(69, idSubtipoInmueble);
        }

        EntityManager entityManager = Utils.crearEntityManager();

        // Realizammos la busqueda
        Par<Exception, List<Inmueble>> resBusInm = controladorInmueble.buscarInmuebles(resBusTipCon.getSegundo(),resBusMunicipio.getSegundo(), paramsAtributos, entityManager);

        // Ocurrio un error al obtener los datos de los inmuebles
        if (resBusInm.getPrimero() != null){
            return new Par<>(resBusInm.getPrimero(), null);
        }

        // No tenemos datos con los criterios solicitados
        if (resBusInm.getSegundo().size() == 0){
            return new Par<>(new Exception("No hay datos disponibles"), null);
        }

        return guardarSolicitud(resBusTipCon.getSegundo(), usuario, resBusInm.getSegundo(), entityManager);
    }

    private Par<Exception, String> guardarSolicitud(TipoContrato tipoContrato, Usuario usuario, List<Inmueble> inmuebles, EntityManager entityManager){

        ControladorInforme controladorInforme = new ControladorInforme();
        ControladorInformeInmueble controladorInformeInmueble = new ControladorInformeInmueble();
        EntityTransaction transaction = entityManager.getTransaction();

        // Obtenemos la fecha de generacion de la solicitud
        long fechaCreacionSolicitud = ZonedDateTime.now(ZoneId.of( "Europe/Madrid" )).toInstant().toEpochMilli();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-HH_mm_ss");
        Date date = new Date(fechaCreacionSolicitud);
        String fechaCreacionTexto = simpleDateFormat.format(date);

        // Guardamos los metadatos del informe
        transaction.begin();
        Informe informe = new Informe(usuario, tipoContrato, "informe_" + fechaCreacionTexto, fechaCreacionSolicitud);
        Par<Exception, Informe> resGuardInf = controladorInforme.guardarInforme(informe,entityManager);
        if (resGuardInf.getPrimero() != null){
            transaction.rollback();
            entityManager.close();
            return new Par<>(resGuardInf.getPrimero(), null);
        }

        // Guardamos los inmuebles que estaran asignados al informe
        int i = 0;
        for (Inmueble inmueble : inmuebles){

            if (i == Constantes.TAMANIO_BATCH_HIBERNATE){
                i = 0;
                entityManager.flush();
                entityManager.clear();
            }

            InformeInmueble informeInmueble = new InformeInmueble(inmueble,informe);
            Par<Exception, InformeInmueble> resGuardInfInm = controladorInformeInmueble.guardarInformeInmueble(informeInmueble, entityManager);
            if (resGuardInfInm.getPrimero() != null){
                transaction.rollback();
                entityManager.close();
                resGuardInfInm.getPrimero().printStackTrace();
                return new Par<>(resGuardInfInm.getPrimero(), null);
            }

            i++;
        }

        transaction.commit();
        entityManager.close();
        return new Par<>(null, "Se ha generado la solicitud exitosamente");
    }

}
