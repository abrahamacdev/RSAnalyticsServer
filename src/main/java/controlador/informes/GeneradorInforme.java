package controlador.informes;

import controlador.analiticas.AbstractAnalitica;
import controlador.analiticas.AnaliticaVivienda;
import controlador.managers.ControladorNotificacion;
import controlador.managers.informes.ControladorInforme;
import io.reactivex.rxjava3.core.Observable;
import kotlin.text.Charsets;
import modelo.pojo.rest.Notificacion;
import modelo.pojo.rest.Usuario;
import modelo.pojo.scrapers.Informe;
import modelo.pojo.scrapers.Inmueble;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.tinylog.Logger;
import utilidades.Constantes;
import utilidades.Par;
import utilidades.Utils;
import utilidades.inmuebles.TipoInmueble;
import utilidades.scrapers.TipoContrato;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Para generar informes tenemos que tener permisos para acceder al directorio
 * "{@link utilidades.Constantes#RUTA_DIRECTORIO_RSANALYTICS}{@link utilidades.Constantes#RUTA_DIRECTORIO_PLANTILLAS}"
 */
public class GeneradorInforme {

    private ControladorInforme controladorInforme;
    private ControladorNotificacion controladorNotificacion;
    private ChromeDriver chromeDriver;

    private ExecutorService piscinaHilos;

    public GeneradorInforme(ExecutorService piscinaHilos){
        this.controladorInforme = new ControladorInforme();
        this.controladorNotificacion = new ControladorNotificacion();
        this.piscinaHilos = piscinaHilos;
        init();
    }

    private void init(){

        ChromeOptions cOptions = new ChromeOptions();
        cOptions.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
        cOptions.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true);
        cOptions.setCapability(CapabilityType.ELEMENT_SCROLL_BEHAVIOR, true);
        cOptions.addArguments("--disable-infobars");
        cOptions.addArguments("--disable-extensions");
        cOptions.addArguments("--disable-notifications");
        cOptions.addArguments("--disable-session-crashed-bubble");
        cOptions.addArguments("--enable-automation");
        cOptions.addArguments("--disable-save-password-bubble");
        cOptions.addArguments("--kiosk-printing");
        cOptions.addArguments("--test-type");
        cOptions.addArguments("--no-sandbox");

        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("printing.default_destination_selection_rules",
                "{ \"kind\": \"local\", \"idPattern\": \".*\", \"namePattern\": \"Save as PDF\" }");
        prefs.put("printing.print_header_footer", false);
        prefs.put("printing.allowed_background_graphics_modes", true);
        prefs.put("download.prompt_for_download", false);
        prefs.put("savefile.default_directory", Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_TEMPORAL);

        cOptions.setExperimentalOption("prefs", prefs);

        chromeDriver = new ChromeDriver(cOptions);
    }


    public void comenzar(){

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                while (true){

                    Par<Exception, Informe> resBusInf = controladorInforme.obtenerInformeMasViejoPorRealizar();

                    // No hay informes pendientes
                    if (resBusInf.ambosSonNulos()){
                        Utils.esperar(Constantes.ESPERA_SI_NO_HAY_INFORMES.getPrimero(), Constantes.ESPERA_SI_NO_HAY_INFORMES.getSegundo());
                    }

                    // Obtenemos el informe y lo procesaremos
                    else if (!resBusInf.segundoEsNulo()){

                        Informe informe = resBusInf.getSegundo();
                        Usuario usuario = informe.getUsuario();

                        // Cromprobamoos que existe un directorio de informes del usuarioo que solicito el infoorme
                        if (existeDirInformesDelUsuario(usuario)){
                            generarInforme(informe);

                            // Limpiamos el directorio temporal
                            limpiarDirTemporal();
                        }
                    }
                }
            }
        };

        // Lanzamos el generador de forma asíncrona
        piscinaHilos.submit(runnable);

    }

    private void generarInforme(Informe informe){

        JSONObject jsonInforme = crearJsonInforme(informe);

        if (jsonInforme == null){
            Logger.error("No se ha podido generar los datos del informe");
            return;
        }

        if (!guardarJsonInformeTemporal(jsonInforme)){
            Logger.error("Ocurrio un error mientras se generaba un informe");
            return;
        }

        if (!copiarPlantillaHTMLTemporal()){
            Logger.error("Ocurrio un error mientras se generaba un informe");
            return;
        }

        // Abrimos el html en Selenium para parsear los datos del jsonn y generar un precioso informe
        chromeDriver.get("file://" + Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_TEMPORAL + "/index.html");

        WebElement fileInput = chromeDriver.findElement(By.id("fileInput"));
        fileInput.sendKeys(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_TEMPORAL + "/data.json");

        // Esperamos a que halla un div con el id "completo"
        Utils.esperarA(null, new Function<Object, Boolean>() {
            @Override
            public Boolean apply(Object o) {

                try {
                    WebElement element = chromeDriver.findElementById("completo");

                }catch (Exception e){
                    return false;
                }

                return true;
            }
        });


        // Imprimimos el archivo
        chromeDriver.executeScript("window.print()");

        // Movemos el pdf generado a la ruta de informes del usuario
        if (!moverInformeGenerado(informe)){
            Logger.error("Ocurrio un error mientras se generaba un informe");
            return;
        }

        // Movemos el json del informe a la ruta raw del usuario
        if (!moverJsonInformeTemporal(informe)){
            Logger.error("Ocurrio un error mientras se generaba un informe");
            return;
        }

        // Finalizamos la creación del informe
        finalizarCreacionInforme(informe);
    }

    private JSONObject crearJsonInforme(Informe informe){

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        Par<Exception, List<Inmueble>> resBusInms = controladorInforme.obtenerTodosLosInmueblesDelInforme(informe, entityManager);

        if (!resBusInms.primeroEsNulo()){
            return null;
        }

        // Creamos un oobservable con la lista de inmuebles
        Observable<Par<Inmueble,Map<String, Object>>> inmuebleObservable = Observable.fromIterable(prepararDatosObservable(resBusInms.getSegundo()));
        AbstractAnalitica abstractAnalitica = null;

        // Cerramos la sesion
        entityTransaction.commit();
        entityManager.close();

        TipoInmueble tipoInmueble = resBusInms.getSegundo().get(0).getTipoInmueble().tipoInmuebleAsEnum();
        TipoContrato tipoContrato = informe.getTipoContrato().tipoContratoAsEnum();
        switch (tipoInmueble){

            case VIVIENDA:
                abstractAnalitica = new AnaliticaVivienda(inmuebleObservable, tipoInmueble, tipoContrato);
                break;
        }

        // No tenemos ninguna analitica para el tipo de inmueble solicitados
        if (abstractAnalitica == null){
            return null;
        }

        // Generamos las analiticas
        abstractAnalitica.generarAnalitica();

        return abstractAnalitica.getJsonFinal();
    }

    private void finalizarCreacionInforme(Informe informe){

        // Establecemos la fecha en la que se ha creado el informe
        informe.setFechaRealizacion(ZonedDateTime.now(ZoneId.of( "Europe/Madrid" )).toInstant().toEpochMilli());

        EntityManager entityManager = Utils.crearEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.begin();

        boolean ocurrioError = false;

        // Actualizamos el estado del informe en la BBDD
        boolean informeActualizado = actualizarEstadoInforme(informe, entityManager);

        // Si se ha actualizado, crearemos una notificacion
        if(informeActualizado){
            boolean notiCreada = crearNotificacionInformeGenerado(informe, entityManager);
            ocurrioError = !notiCreada;
        }

        // Noo se pudo actualizar el informe
        else {
            ocurrioError = true;
        }

        // Ocurrio un error en algno de los procesos
        if (ocurrioError){
            entityTransaction.rollback();
        }

        // Hacemos efectivos los cambios
        else {
            entityTransaction.commit();
        }

        entityManager.close();
    }

    private boolean actualizarEstadoInforme(Informe informe, EntityManager entityManager){

        // Establecemos la ruta en la que está almacenado el informe
        informe.setRutaArchivo(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_INFORMES + "/" + informe.getUsuario().getId() +
                Constantes.RUTA_DIRECTORIO_INFORMES_GENERADOS + "/" + informe.getNombre() + ".pdf");

        int res = controladorInforme.actualizarInforme(informe, entityManager);

        return res == 1;
    }

    private boolean crearNotificacionInformeGenerado(Informe informe, EntityManager entityManager){

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
        calendar.setTimeInMillis(informe.getFechaCreacionSolicitud());

        Notificacion notificacion = new Notificacion();
        notificacion.setReceptor(informe.getUsuario());
        notificacion.setMensaje("Ya está disponible el informe que solicitaste el día " +  calendar.get(Calendar.DAY_OF_MONTH) + " de " +
                Utils.mes2Texto(calendar.get(Calendar.MONTH)) + " de " + calendar.get(Calendar.YEAR) + " a las " + calendar.get(Calendar.HOUR_OF_DAY) +
                ":" + calendar.get(Calendar.MINUTE));

        int resGuardado = controladorNotificacion.guardarNuevaNotificacion(notificacion);

        return resGuardado == 0;
    }

    private List<Par<Inmueble,Map<String, Object>>> prepararDatosObservable(List<Inmueble> inmuebles){

        try {
            return inmuebles.stream()
                    .map(inmueble -> {
                        Map<String, Object> atributos = inmueble.getAtributos()
                                .stream()
                                .map(atributoInmueble -> {
                                    return new Par<String,Object>(atributoInmueble.getClaveAtributoInmueble().getNombre(), atributoInmueble.getValorActivo());
                                })
                                .collect(Collectors.toMap(par -> par.getPrimero(), par -> par.getSegundo()));
                        return new Par<Inmueble, Map<String,Object>>(inmueble, atributos);
                    })
                    .collect(Collectors.toList());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }



    /**
     * Comprobamos si para un cierto usuario hay un directorio ya creado que contenga informes
     * @param usuario
     * @return
     */
    private boolean existeDirInformesDelUsuario(Usuario usuario){

        File dirGenerados = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS +
                Constantes.RUTA_DIRECTORIO_INFORMES + "/" + usuario.getId() + Constantes.RUTA_DIRECTORIO_INFORMES_GENERADOS);

        File dirRaw = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS +
                Constantes.RUTA_DIRECTORIO_INFORMES + "/" + usuario.getId() + Constantes.RUTA_DIRECTORIO_INFORMES_RAW);

        if (!dirGenerados.exists()){
            if (!dirGenerados.mkdirs()){
                Logger.error("No se ha podido crear el directorio de informes del usuario " + usuario.getId());
                return false;
            }
        }

        if (!dirRaw.exists()){
            if (!dirRaw.mkdirs()){
                Logger.error("No se ha podido crear el directorio de informes del usuario " + usuario.getId());
                return false;
            }
        }

        return true;
    }

    private boolean guardarJsonInformeTemporal(JSONObject jsonObject){

        try {

            File file = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_TEMPORAL + "/data.json");
            FileUtils.writeStringToFile(file, jsonObject.toJSONString(), Charsets.UTF_8);

            return true;

        } catch (IOException e) {
            return false;
        }
    }

    private boolean copiarPlantillaHTMLTemporal(){

        try {

            File dst = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_TEMPORAL + "/index.html");
            File src = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_PLANTILLAS + "/index.html");

            FileUtils.copyFile(src,dst);
            return true;

        } catch (IOException e) {
            return false;
        }

    }

    private boolean moverInformeGenerado(Informe informe){

        try {

            File dirTemp = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_TEMPORAL);
            List<File> archivosTemp = Arrays.asList(dirTemp.listFiles());
            Optional<File> opcionalInformeGenerado = archivosTemp.stream()
                                        .filter(archivos -> FilenameUtils.getExtension(archivos.getName()).equals("pdf"))
                                        .findFirst();

            // Movemos el informe generado a la ruta de informes del usuario
            if (opcionalInformeGenerado.isPresent()){
                File dst = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS +
                        Constantes.RUTA_DIRECTORIO_INFORMES + "/" + informe.getUsuario().getId() + Constantes.RUTA_DIRECTORIO_INFORMES_GENERADOS +
                        "/" + informe.getNombre() + ".pdf");

                FileUtils.moveFile(opcionalInformeGenerado.get(), dst);
                return true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    private boolean moverJsonInformeTemporal(Informe informe){

        try {

            File dirTemp = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_TEMPORAL);
            List<File> archivosTemp = Arrays.asList(dirTemp.listFiles());
            Optional<File> opcionalJsonInforme = archivosTemp.stream()
                    .filter(archivos -> FilenameUtils.getExtension(archivos.getName()).equals("json"))
                    .findFirst();

            // Movemos el informe generado a la ruta de informes del usuario
            if (opcionalJsonInforme.isPresent()){
                File dst = new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS +
                        Constantes.RUTA_DIRECTORIO_INFORMES + "/" + informe.getUsuario().getId() + Constantes.RUTA_DIRECTORIO_INFORMES_RAW +
                        "/" + informe.getNombre() + ".json");

                FileUtils.moveFile(opcionalJsonInforme.get(), dst);
                return true;
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return false;

    }

    private void limpiarDirTemporal(){

        try {
            FileUtils.cleanDirectory(new File(Constantes.RUTA_DIRECTORIO_RSANALYTICS + Constantes.RUTA_DIRECTORIO_TEMPORAL));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
