package controlador.scrapers.scraper.fotocasa;

import controlador.managers.anuncios.ControladorAtributoAnuncio;
import controlador.scrapers.TipoScraper;
import controlador.scrapers.scraper.AbstractScraper;
import controlador.scrapers.OnScraperListener;
import modelo.pojo.Municipio;
import modelo.pojo.Provincia;
import modelo.pojo.scrapers.Anuncio;
import modelo.pojo.scrapers.ClaveAtributoAnuncio;
import modelo.pojo.scrapers.Procedencia;
import modelo.pojo.scrapers.atributo_anuncio.AtributoAnuncio;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarLog;
import net.lightbody.bmp.proxy.CaptureType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.tinylog.Logger;
import utilidades.Constantes;
import utilidades.Par;
import utilidades.Utils;
import utilidades.scrapers.ScrapersUtils;
import utilidades.scrapers.TipoContrato;
import utilidades.scrapers.TipoInmueble;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static utilidades.Constantes.DESCANSO_ENTRE_CICLOS;
import static utilidades.Constantes.DESCANSO_ENTRE_PAGINAS;
import static utilidades.Constantes.DESCANSO_ENTRE_ANUNCIOS;

public class ScraperFotocasa extends AbstractScraper {

    private ChromeDriver chromeDriver;
    private BrowserMobProxy browserMobProxy;
    private Har har;

    private TipoContrato tipoContrato;
    private TipoInmueble tipoInmueble;

    private ControladorAtributoAnuncio controladorAtributoAnuncio;
    private Procedencia procedenciaFotocasa = Procedencia.obtenerProcedenciaConNombre("Fotocasa");
    private Map<String, ClaveAtributoAnuncio> clavesPermitidas = new HashMap<>();

    private DateTimeFormatter formatoFechaAnuncio = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");


    private OnScraperListener onScraperListener;

    public ScraperFotocasa(OnScraperListener onScraperListenerCallback){
        this.onScraperListener = onScraperListenerCallback;
        init();
    }


    private void init(){

        // Establecemos el primer tipo de contrato y el primer tipo de inmueble que obtendremos
        tipoContrato = TipoContrato.COMPRA;
        tipoInmueble = TipoInmueble.VIVIENDA;

        // Obtenemos las claves de los atributos permitidos
        controladorAtributoAnuncio = new ControladorAtributoAnuncio();
        List<ClaveAtributoAnuncio> clavesAtributosAnuncios = controladorAtributoAnuncio.obtenerClavesPosibles();
        if (clavesAtributosAnuncios != null){
            clavesPermitidas = clavesAtributosAnuncios.stream()
                                .collect(Collectors.toMap(
                                        claveAtributoAnuncio -> claveAtributoAnuncio.getNombre(), claveAtributoAnuncio -> claveAtributoAnuncio
                                ));
        }


        // Inicializamos el proxy server
        browserMobProxy = inicializarProxy();
        Proxy proxy = ClientUtil.createSeleniumProxy(browserMobProxy);

        // Inicializamos selenium mediante el chrome driver
        chromeDriver = inicializarChromeDriver(proxy);

        // Guardamos el har que intermediara las conexiones con Fotocasa
        har = browserMobProxy.getHar();
    }

    private BrowserMobProxy inicializarProxy(){

        // HAR Proxy Server
        BrowserMobProxy server = new BrowserMobProxyServer();
        server.start(0);
        server.setHarCaptureTypes(CaptureType.getResponseCaptureTypes());
        server.enableHarCaptureTypes(CaptureType.RESPONSE_CONTENT);
        server.newHar("Fotocasa");

        return server;
    }

    private ChromeDriver inicializarChromeDriver(Proxy proxy){

        // Argumentos
        ArrayList<String> argumentos = new ArrayList<>();
        argumentos.add("--window-size=" + ConstantesFotocasa.ANCHO + "," + ConstantesFotocasa.ALTO);
        argumentos.add("--ignore-certificate-errors");
        argumentos.add("--no-sandbox");

        ChromeOptions options = new ChromeOptions();
        options.addArguments(argumentos);
        options.setCapability("proxy", proxy);

        // Creamos el driver
        return new ChromeDriver(options);
    }


    @Override
    public void comenzar() {

        // Obtendremos los datos en local
        if (Constantes.MODO_PRUEBA){
            scrapearLocal();
        }

        else {
            scrapearInternet();
        }

    }

    private void scrapearInternet(){

        final int[] recuento = new int[]{0};

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Logger.info("Recuento hasta el momento: " + recuento[0]);
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 30000, 30000);

        try {

            boolean continuar = true;

            Date fechaObtencion = new Date();

            while (continuar){

                // Vamos a la primera pagina segun el tipo de contrato y el tipo de inmueble
                irPrimeraPaginaDe(tipoContrato, tipoInmueble, "España");

                // Obtenemos los datos de cada anuncio individual y lo añadimos a la lista
                List<JSONObject> anunciosEnJson = obtenerTodosDatos();
                recuento[0] += anunciosEnJson.size();

                // Avisamos de la recoleccion de una tanda de anuncios
                onScraperListener.onScraped(parsearJsons2Pojos(anunciosEnJson, fechaObtencion), TipoScraper.FOTOCASA);

                do {

                    // Antes de ir a la siguiente pagina esperamos aleatoriamente entre 15 y 40 segundos
                    int segundosRandom = (int) (Math.random() * (DESCANSO_ENTRE_PAGINAS.getSegundo() - DESCANSO_ENTRE_PAGINAS.getPrimero())) + DESCANSO_ENTRE_PAGINAS.getPrimero();
                    Utils.esperar(segundosRandom * 1000);

                    // Vamos a la siguiente pagina
                    irSiguientePagina();

                    // Obtenemos los datos de cada anuncio individual y lo añadimos a la lista
                    anunciosEnJson = obtenerTodosDatos();

                    recuento[0] += anunciosEnJson.size();

                    // Avisamos de la recoleccion de una tanda de anuncios
                    onScraperListener.onScraped(parsearJsons2Pojos(anunciosEnJson, fechaObtencion), TipoScraper.FOTOCASA);

                }while (haySiguientePagina());

                // Rotamos los parametros de busqueda
                boolean parametrosRotados = rotarParametros();

                // Hemos acabado un bucle completo (todos los contratos con todos los inmuebles),
                // esperaremos una/s horillas
                if (!parametrosRotados){
                    tipoContrato = TipoContrato.obtenerPorIndice(0);
                    tipoInmueble = TipoInmueble.obtenerPorIndice(0);
                    int horasRandom = (int) (Math.random() * (DESCANSO_ENTRE_CICLOS.getSegundo() - DESCANSO_ENTRE_CICLOS.getPrimero())) + DESCANSO_ENTRE_CICLOS.getPrimero();

                    // Dormimos el hilo unas horitas
                    Utils.esperar(horasRandom * 60 * 60 * 1000);

                    // Reseteamos la fecha de obtencion
                    fechaObtencion = new Date();
                }
            }

        }catch (Exception e){

            e.printStackTrace();

            // Detenemos toodo y avisamos de que ocurrio un error
            timer.cancel();
            detener();
            onScraperListener.onError(e,TipoScraper.FOTOCASA);
        }

        // Detenemos toodo y avisamos de que terminamos exitosamente la ejecucion
        timer.cancel();
        detener();
        onScraperListener.onTerminado(TipoScraper.FOTOCASA);
    }

    private void scrapearLocal(){

        File ruta = new File(Constantes.RUTA_JSONS_MODO_PRUEBA);

        Date fechaObtencion = new Date();

        List<JSONObject> jsons = Arrays.stream(ruta.listFiles())
                .filter(archivo -> FilenameUtils.getExtension(archivo.getName()).equals("json"))
                .map(archivo -> {

                    try {
                        return FileUtils.readFileToString(archivo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return "";
                })
                .map(stringJson -> {
                    try {
                        return (JSONObject) new JSONParser().parse(stringJson);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return new JSONObject();
                })
                .filter(json -> json.keySet().size() > 0)
                .collect(Collectors.toList());

        Logger.info("Comenzamos a scrapear");

        int salto = Constantes.TAMANIO_BATCH_HIBERNATE;
        int actual = 0;
        int cachos = jsons.size() / salto;
        int restante = jsons.size() % salto;

        for (int i=0; i<cachos; i++){

            List<JSONObject> parte = jsons.subList(actual, actual + salto);
            List<Anuncio> anuncios = parsearJsons2Pojos(parte, fechaObtencion);

            Logger.info("Parseados");

            onScraperListener.onScraped(anuncios, TipoScraper.FOTOCASA);

            actual += salto;

            Logger.info("Siguiente pagina");
        }

        List<JSONObject> parte = jsons.subList(actual, actual + restante);

        onScraperListener.onScraped(parsearJsons2Pojos(parte, fechaObtencion), TipoScraper.FOTOCASA);

        // Detenemos el scraper
        detener();

        onScraperListener.onTerminado(TipoScraper.FOTOCASA);
    }



    private void irPrimeraPaginaDe(TipoContrato tipoContrato, TipoInmueble tipoInmueble, String zona){

        By byTipoContrato = null;
        By byTipoInmueble = new By.ByCssSelector("div.re-SearchPropertyDropdownSelector > select");
        int idxTipoInmueble = 1;

        switch (tipoContrato){

            case COMPRA:
                byTipoContrato = new By.ByCssSelector("div.re-Search-selectorContainer.re-Search-selectorContainer--buy>label");
                break;

            case ALQUILER:
                byTipoContrato = new By.ByCssSelector("div.re-Search-selectorContainer.re-Search-selectorContainer--rent>label");
                break;
        }

        switch (tipoInmueble){

            case VIVIENDA:
                idxTipoInmueble = 2;
                break;
        }

        // Vamos a la pagina principal
        irPaginaInicio();

        // Esperamos a que cargue la pagina principal
        Utils.esperarA(null, 5000, new Function<Object, Boolean>() {

            @Override
            public Boolean apply(Object object) {

                try {

                    return chromeDriver.findElement(byTipoInmueble) != null;

                }catch (Exception e){

                }

                return false;
            }
        });

        // Seleccionamos el tipo de contrato
        chromeDriver.findElement(byTipoContrato).click();

        // Seleccionamos la opcion del dropdown menu
        Select selectTipoInmueble = new Select(chromeDriver.findElement(byTipoInmueble));
        selectTipoInmueble.selectByValue(String.valueOf(idxTipoInmueble));

        // Escribimos la zona de busqueda
        WebElement element = chromeDriver.findElementByClassName("sui-AtomInput-input");
        element.sendKeys(zona);
        element.sendKeys(Keys.RETURN);

        // Esperamos a que hallamos cargado la primera pagina de destino
        Utils.esperarA(null, 5000, new Function<Object, Boolean>() {
            @Override
            public Boolean apply(Object object) {

                try {

                    // Intentamos buscar un elemento de las paginas de busqueda
                    WebElement tempElement = chromeDriver.findElementByCssSelector("span.re-SearchTitle-count");
                    if (tempElement != null && tempElement.isDisplayed()){
                        return true;
                    }

                }catch (NoSuchElementException such){
                    // No pasa nada, tranquilo
                }
                catch (Exception e){
                    // TODO Descomentar ultima linea y comentar las otras
                    e.printStackTrace();
                    //Logger.error(e.getLocalizedMessage());
                }

                return false;
            }
        });
    }

    private void irPaginaInicio(){
        chromeDriver.get(ConstantesFotocasa.URL_BASE_FOTOCASA);
    }

    private boolean haySiguientePagina(){

        Utils.esperarA(null, 5000, new Function<Object, Boolean>() {
            @Override
            public Boolean apply(Object object) {

                try {

                    // Comprobamos que esten los elementos de paginacion al final de la pagina
                    return chromeDriver.findElementsByCssSelector("li.sui-PaginationBasic-item>a").size() > 1;

                }catch (NoSuchElementException such){

                }catch (Exception e){
                    e.printStackTrace();
                }

                return false;
            }
        });

        List<WebElement> paginadores = chromeDriver.findElementsByCssSelector("li.sui-PaginationBasic-item");
        int visibles = 0;

        for (WebElement paginador : paginadores) {
            if (paginador.isDisplayed()){
                visibles++;
            }
        }

        // Debera de haber como minimo 3 indicadores (<-,x,->)
        return visibles >= 3;
    }

    private void irSiguientePagina(){

        System.out.println("Vamos a la siguiente pagina");

        // Clickeamos el boton de ir a la siguiente pagina
        List<WebElement> paginacion = chromeDriver.findElementsByCssSelector("li.sui-PaginationBasic-item>a");
        paginacion.get(paginacion.size()-1).click();

        Utils.esperarA(null, 5000, new Function<Object, Boolean>() {
            @Override
            public Boolean apply(Object object) {

                try {

                    // Comprobamos que esten los elementos de paginacion al final de la pagina
                    return chromeDriver.findElementsByCssSelector("li.sui-PaginationBasic-item>a").size() > 1;

                }catch (NoSuchElementException such){

                }catch (Exception e){
                    e.printStackTrace();
                }

                return false;
            }
        });
    }



    private List<JSONObject> obtenerListadosAnuncios(){

        HarLog log = har.getLog();
        List<JSONObject> listadoAnuncios = log.getEntries().stream()
                .filter((harEntry) -> {
                    return Utils.dominioCoincideCon(harEntry.getRequest().getUrl(), "api.fotocasa.es");
                })
                .map((harEntry) -> {
                    try {
                        return ((JSONObject) new JSONParser().parse(harEntry.getResponse().getContent().getText()));
                    } catch (Exception e) {
                        return new JSONObject();
                    }
                })
                .filter(jsonObject -> jsonObject.containsKey("breadcrumb"))
                .collect(Collectors.toList());

        // Limpiamos el log
        har.setLog(new HarLog());

        return listadoAnuncios;
    }

    private List<JSONObject> obtenerResumenListadosAnuncios(List<JSONObject> listadosAnuncios){
        return listadosAnuncios.stream()
                .map((root) -> {
                    return ((JSONArray)root.get("realEstates"));
                })
                .flatMap(jsonArray -> Arrays.stream(jsonArray.toArray()))
                .map(singleObject -> ((JSONObject)singleObject))
                .collect(Collectors.toList());
    }

    private JSONObject obtenerDatosDetalleAnuncio(JSONObject jsonDetalle){

        // TODO Ver que hacer
        Utils.esperar(DESCANSO_ENTRE_ANUNCIOS * 1000);

        long idInmueble = (long) jsonDetalle.get("id");

        String urlDetalleAnuncio = "https://api.fotocasa.es/PropertySearch/Property?culture=es-ES&locale=es-ES&transactionType=" +
                convertirTipoContrato2Int() + "&periodicityId=0&id=" + idInmueble;


        HttpClient client = HttpClients.custom().build();
        RequestBuilder requestBuilder = RequestBuilder.get().setUri(urlDetalleAnuncio);

        HttpUriRequest request = requestBuilder.build();

        try {
            HttpResponse response = client.execute(request);
            String html = EntityUtils.toString(response.getEntity());

            JSONObject root = (JSONObject) new JSONParser().parse(html);

            return root;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JSONObject();
    }

    private List<JSONObject> obtenerTodosDatos(){

        Logger.info("Obteniendo los datos en detalle...");

        List<JSONObject> listadosAnuncios = obtenerListadosAnuncios();
        List<JSONObject> resumenListadosAnuncios = obtenerResumenListadosAnuncios(listadosAnuncios);

        // TODO Eliminar
        List<JSONObject> listadoEnDetalle = new ArrayList<>(resumenListadosAnuncios.size());
        long tiempo = Utils.cronometrarConNanos(() -> {
                    listadoEnDetalle.addAll(resumenListadosAnuncios.stream()
                        .map(resumen -> obtenerDatosDetalleAnuncio(resumen))
                        .collect(Collectors.toList())
                    );
        });
        Logger.info("Nos ha tomado " + Utils.nano2Sec(tiempo) + "s obtener los datos");
        return listadoEnDetalle;
    }

    private List<Anuncio> parsearJsons2Pojos(List<JSONObject> jsonAnuncios, Date fechaObtencion){

        // TODO Eliminar en produccion. Almacena los jsons que obtengamos para poder disponer de ellos el dia de la presentacion
        if (!Constantes.MODO_PRUEBA){
            Utils.guardarJsons2Archivo(Constantes.RUTA_JSONS_MODO_PRUEBA, true, jsonAnuncios);
        }

        return jsonAnuncios.stream()
                .map(anuncio -> parsearJson2Pojo(anuncio))
                .map(anuncio -> {
                    anuncio.setFechaObtencion(fechaObtencion);
                    return anuncio;
                })
                .collect(Collectors.toList());
    }

    private Anuncio parsearJson2Pojo(JSONObject jsonAnuncio){

        Anuncio anuncio = new Anuncio();
        anuncio.setProcedencia(procedenciaFotocasa);

        List<AtributoAnuncio> claveAtributoAnuncios = new ArrayList<>();

        // Obtenemos todos los atributos del anuncio
        claveAtributoAnuncios.addAll(obtenerAtributosPrincipales(anuncio, jsonAnuncio));
        claveAtributoAnuncios.addAll(obtenerAtributosExtras(anuncio, jsonAnuncio));

        // Añadimos todoos los atributos al anuncio
        anuncio.getAtributos().addAll(claveAtributoAnuncios);

        // Obtenemos el municipio del anuncio
        anuncio.setMunicipio(obtenerMunicipio(jsonAnuncio));

        return anuncio;
    }


    private List<AtributoAnuncio> obtenerAtributosPrincipales(Anuncio anuncio, JSONObject jsonAnuncio){

        HashMap<String, Object> tempAtributos = new HashMap<>(17);

        // Id del anuncio
        Double idAnuncio = jsonAnuncio.containsKey("id") ? ((Long) jsonAnuncio.get("id")).doubleValue() : null;
        tempAtributos.put("Id Anuncio", idAnuncio);

        JSONObject direccion = (JSONObject) ObjectUtils.defaultIfNull(jsonAnuncio.get("address"), new JSONObject());
        String codigoPostal = (String) direccion.get("zipCode");
        tempAtributos.put("CP", codigoPostal);

        // Cantidad de imagenes
        JSONArray imagenes = (JSONArray) ObjectUtils.defaultIfNull(jsonAnuncio.get("multimedias"), new JSONArray());
        if (imagenes.size() > 0){
            tempAtributos.put("Numero Imagenes", ((Integer)imagenes.size()).doubleValue());
        }

        // Url del anuncio
        JSONObject urlsAnuncio = (JSONObject) ObjectUtils.defaultIfNull(jsonAnuncio.get("detail"), new JSONObject());
        String urlEspaniol = (String) urlsAnuncio.get("es");
        tempAtributos.put("Url Anuncio", urlEspaniol);

        // Fecha de publicacion del anuncio
        String tempFechaPublicacion = (String) jsonAnuncio.get("date");
        Double fecha = null;
        if (tempFechaPublicacion != null){
            tempFechaPublicacion = tempFechaPublicacion.substring(0,19);
            LocalDateTime localFecha = LocalDateTime.parse(tempFechaPublicacion, formatoFechaAnuncio);
            fecha = ((Long) localFecha.atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()).doubleValue();
        }
        tempAtributos.put("Fecha publicacion", fecha);

        // Precio
        JSONArray transacciones = (JSONArray) ObjectUtils.defaultIfNull(jsonAnuncio.get("transactions"), new JSONArray());
        transacciones.stream()
                .filter(temp -> {
                    JSONObject jsonTransaccion = (JSONObject) temp;
                    Integer idTransaccion = ((Long) ObjectUtils.defaultIfNull(jsonTransaccion.get("transactionTypeId"), -1)).intValue();
                    return convertirTipoContrato2Int() == idTransaccion;
                })
                .map(temp -> {
                    JSONObject jsonTransaccion = (JSONObject) temp;
                    JSONArray precios = (JSONArray) ObjectUtils.defaultIfNull(jsonTransaccion.get("value"), new JSONArray());
                    Double precio = precios.size() > 0 ? ((Long) precios.get(0)).doubleValue() : null;
                    return precio;
                })
                .filter(precio -> precio != null)
                .forEach(precio -> tempAtributos.put("Precio", precio));

        // Tipo de contrato
        tempAtributos.put("Tipo Contrato", Utils.capitalize(tipoContrato.name().toLowerCase()));

        // Datos de localizacion (ciudad, provincia... etc)
        tempAtributos.putAll(obtenerLatitudLongitud(jsonAnuncio));

        // M2, Antiguedad... etc
        int caracteristicasBuscadas = 2;
        JSONArray caracteristicas = (JSONArray) ObjectUtils.defaultIfNull(jsonAnuncio.get("features"), new JSONArray());
        for (Object caracteristica : caracteristicas) {
            JSONObject jsonCaracteristica = (JSONObject) caracteristica;

            // M2
            if (jsonCaracteristica.containsKey("surface")){
                Long m2 = (Long) jsonCaracteristica.get("surface");
                tempAtributos.put("M2", ((Long) m2).doubleValue());
                caracteristicasBuscadas--;
            }

            // Antiguedad
            if (jsonCaracteristica.containsKey("antiquity")){
                Long antiguedad = (Long) jsonCaracteristica.get("antiquity");
                tempAtributos.put("Antiguedad", ((Long) antiguedad).doubleValue());
                caracteristicasBuscadas--;
            }

            // Evitamos segguir iterando
            if (caracteristicasBuscadas == 0){
                break;
            }
        }

        // Datos del anunciante
        tempAtributos.putAll(obtenerAnunciante(jsonAnuncio));

        // Tipo de inmueble (piso, chalet, casa adosada... etc)
        Integer tipoId = ((Long) ObjectUtils.defaultIfNull(jsonAnuncio.get("typeId"), -1)).intValue();
        Integer subtipoId = ((Long) ObjectUtils.defaultIfNull(jsonAnuncio.get("subtypeId"), -1)).intValue();
        String nombreTipoInmueble = convertirIdsTipoInmueble2Texto(tipoId, subtipoId);
        tempAtributos.put("Tipo Inmueble", nombreTipoInmueble);
        tempAtributos.put("Id Tipo Inmueble", tipoId.doubleValue());
        tempAtributos.put("Id Subtipo Inmueble", subtipoId.doubleValue());

        // Coleccionamos todas las caracteristicas generales obligatorias
        List<AtributoAnuncio> atributosAnuncio = tempAtributos.keySet()
                .stream()
                .filter(key -> clavesPermitidas.containsKey(key) && tempAtributos.get(key) != null)
                .map(key -> {

                    AtributoAnuncio atributoAnuncio = new AtributoAnuncio(anuncio, clavesPermitidas.get(key));
                    Object value = tempAtributos.get(key);

                    if (value instanceof Double){
                        atributoAnuncio.setValorNumerico((Double) value);
                    }

                    else {
                        atributoAnuncio.setValorCadena((String) value);
                    }

                    return atributoAnuncio;
                })
                .collect(Collectors.toList());

        // Agregamos caracteristicas obligatorias extras
        switch (tipoInmueble){

            case VIVIENDA:
                atributosAnuncio.addAll(obtenerAtributosVivienda(anuncio, jsonAnuncio, ScrapersUtils.viviendaEsPiso(subtipoId)));
        }

        return atributosAnuncio;
    }

    private List<AtributoAnuncio> obtenerAtributosExtras(Anuncio anuncio, JSONObject jsonAnuncio){

        HashMap<String, Object> mapExtras = new HashMap<>();

        // Caracteristicas del objeto "otherFeatures"
        JSONArray extras = (JSONArray) ObjectUtils.defaultIfNull(jsonAnuncio.get("otherFeatures"), new JSONArray());
        for (Object extra : extras) {
            JSONObject tempExtra = (JSONObject) extra;
            Set<String> keys = tempExtra.keySet();
            keys.stream()
                    .forEach(key -> mapExtras.put(String.valueOf(tempExtra.get(key)), 1.0));
        }

        // Caracteristicas de "features"
        int buscados = 1;
        JSONArray caracteristicas = (JSONArray) ObjectUtils.defaultIfNull(jsonAnuncio.get("features"), new JSONArray());

        for (Object temp : caracteristicas){
            JSONObject  jsonCaracteristica = (JSONObject) temp;

            // Orientacion
            if (jsonCaracteristica.containsKey("orientation")){
                Integer tipoOrientacion = ((Long)jsonCaracteristica.get("orientation")).intValue();
                mapExtras.put("Orientacion", convertirTipoOrientacion2Texto(tipoOrientacion));
                Integer idOrientacion = convertirIdOrientacion(tipoOrientacion);
                mapExtras.put("Id Orientacion", idOrientacion != null ? idOrientacion.doubleValue() : null);
                buscados--;
            }

            // Evitamos seguir iterando
            if (buscados == 0){
                break;
            }
        }

        // Parseamos cada tupla del map a un objeeto "AtributoInmueble" y lo añadimos a la lista
        return mapExtras.keySet()
                .stream()
                .filter(key -> clavesPermitidas.containsKey(key) && mapExtras.get(key) != null)
                .map(key -> clavesPermitidas.get(key))
                .map(claveAtributoAnuncio -> {

                    AtributoAnuncio atributoAnuncio = new AtributoAnuncio(anuncio, claveAtributoAnuncio);
                    Object valor = mapExtras.get(claveAtributoAnuncio.getNombre());

                    if (valor instanceof Double){
                            atributoAnuncio.setValorNumerico((Double) valor);
                        }

                    else {
                            atributoAnuncio.setValorCadena((String) valor);
                        }

                    return atributoAnuncio;
                })
                .collect(Collectors.toList());
    }

    private List<AtributoAnuncio> obtenerAtributosVivienda(Anuncio anuncio, JSONObject jsonAnuncio, boolean esPiso){

        HashMap<String, Object> tempAtributos = new HashMap<>();

        // Baños y habitaciones
        JSONArray caracteristicas = (JSONArray) jsonAnuncio.getOrDefault("features", new JSONArray());
        int porBuscar = 2;
        if (caracteristicas != null){
            // Recorremmos el listado de las caracteristicas en busca de las esperadas
            for (Object json : caracteristicas){
                JSONObject caracteristica = (JSONObject) json;

                // Obtenemos el numero de baños
                if (caracteristica.containsKey("bathrooms")){
                    tempAtributos.put("Banos", ((Long) caracteristica.get("bathrooms")).doubleValue());
                    porBuscar--;
                }

                // Obtenemos el numero de habitaciones
                if (caracteristica.containsKey("rooms")){
                    tempAtributos.put("Numero Habitaciones", ((Long) caracteristica.get("rooms")).doubleValue());
                    porBuscar--;
                }

                // Evitamos seguir recorriendo el JSON a lo tonto
                if (porBuscar == 0){
                    break;
                }
            }
        }

        // Obtenemos los datos energeticos
        JSONObject propiedadesEnergeticas = (JSONObject) jsonAnuncio.get("propertyEnergyCertificate");
        if (propiedadesEnergeticas != null){
            double consumo = ((Long) propiedadesEnergeticas.getOrDefault("energyEfficiencyRatingTypeId", 0)).doubleValue();
            tempAtributos.put("Consumo", Utils.convertirCertificadoEnergetico2Letra(consumo));

            double emisiones = ((Long) propiedadesEnergeticas.getOrDefault("environmentImpactRatingTypeId", 0)).doubleValue();
            tempAtributos.put("Emisiones", Utils.convertirCertificadoEnergetico2Letra(emisiones));
        }

        // Si es un piso obtendremos la planta a la que pertenece
        if (esPiso){
            JSONArray listaCaracteristicas = (JSONArray) ObjectUtils.defaultIfNull(jsonAnuncio.get("featuresList"), new JSONArray());

            for (int i=0; i<listaCaracteristicas.size(); i++){

                JSONObject caracteristica = (JSONObject) listaCaracteristicas.get(i);
                String etiqueta = (String) ObjectUtils.defaultIfNull(caracteristica.get("label"),"");

                // Obtenemos el numero de la planta
                if (etiqueta.equals("floor")){
                    Double planta = caracteristica.containsKey("id") ? ((Long) caracteristica.get("id")).doubleValue() : null;
                    tempAtributos.put("Planta", planta);
                    break;
                }
            }
        }

        return tempAtributos.keySet()
                .stream()
                .filter(key -> clavesPermitidas.containsKey(key))
                .map(key -> clavesPermitidas.get(key))
                .map((claveAtributoAnuncio) -> {

                    // OObtenemos el valor del atributo y creamos el objeto "Atributo"
                    Object valorAtributo = tempAtributos.get(claveAtributoAnuncio.getNombre());
                    AtributoAnuncio atributoAnuncio = new AtributoAnuncio(anuncio,claveAtributoAnuncio);

                    if (valorAtributo instanceof Number){
                        atributoAnuncio.setValorNumerico((Double) valorAtributo);
                    }

                    else {
                        atributoAnuncio.setValorCadena((String) valorAtributo);
                    }

                    return atributoAnuncio;
                })
                .collect(Collectors.toList());
    }


    private HashMap<String, Object> obtenerLatitudLongitud(JSONObject jsonAnuncio){

        HashMap<String, Object> datosLocalizacion = new HashMap<>(5);
        List<Par<String, Object>> temporales = new ArrayList<>(5);

        JSONObject direccion = (JSONObject) ObjectUtils.defaultIfNull(jsonAnuncio.get("address"), new JSONObject());

        // Latitud y longitud
        JSONObject coordenadas = (JSONObject) ObjectUtils.defaultIfNull(direccion.get("coordinates"), new JSONObject());
        Double latitud = (Double) coordenadas.get("latitude");
        Double longitud = (Double) coordenadas.get("longitude");
        datosLocalizacion.put("Latitud", latitud);
        datosLocalizacion.put("Longitud", longitud);

        return datosLocalizacion;
    }

    private HashMap<String, Object> obtenerAnunciante(JSONObject jsonAnuncio){

        HashMap<String, Object> datosAnunciante = new HashMap<>(5);

        JSONObject jsonDatosAnunciante = (JSONObject) ObjectUtils.defaultIfNull(jsonAnuncio.get("advertiser"), new JSONObject());

        // Nombre del anunciante
        String nombre = jsonDatosAnunciante.get("contactName") != null ? ((String) jsonDatosAnunciante.get("contactName")).trim() : null;
        datosAnunciante.put("Nombre Anunciante", nombre);

        // Id del anunciante
        String idAnunciante = jsonDatosAnunciante.get("clientId") != null ? (((Long) jsonDatosAnunciante.get("clientId")).toString()).trim() : null;
        datosAnunciante.put("Id Anunciante", idAnunciante);

        // Telefono de contacto
        String telefono = jsonDatosAnunciante.get("phone") != null ? ((String) jsonDatosAnunciante.get("phone")).trim() : null;
        datosAnunciante.put("Numero de Contacto", telefono);

        // Referencia del anunciante
        String referenciaAnunciante = jsonDatosAnunciante.get("reference") != null ? ((String) jsonDatosAnunciante.get("reference")).trim() : null;
        datosAnunciante.put("Referencia Anunciante", referenciaAnunciante);

        // Tipo de anunciante
        Integer idTipoAnunciante = ((Long) ObjectUtils.defaultIfNull(jsonDatosAnunciante.get("typeId"), -1)).intValue();
        String tempTipoAnunciante = convertirIdTipoAnunciante2Texto(idTipoAnunciante);
        String tipoAnunciante = tempTipoAnunciante != null ? tempTipoAnunciante.trim() : null;
        datosAnunciante.put("Tipo Anunciante", tipoAnunciante);

        // Url detalle anunciante
        JSONObject jsonLogo = (JSONObject) ObjectUtils.defaultIfNull(jsonDatosAnunciante.get("logo"), new JSONObject());
        JSONObject jsonUrl = (JSONObject) ObjectUtils.defaultIfNull(jsonLogo.get("url"), new JSONObject());
        String url = jsonUrl.get("es-ES") != null ? ((String) jsonUrl.get("es-ES")).trim() : null;
        datosAnunciante.put("Url Anunciante", url);

        return datosAnunciante;
    }

    private Municipio obtenerMunicipio(JSONObject jsonAnuncio){

        JSONObject direccion = (JSONObject) ObjectUtils.defaultIfNull(jsonAnuncio.get("address"), new JSONObject());

        // Provincia
        JSONObject localizacion = (JSONObject) ObjectUtils.defaultIfNull(direccion.get("location"), new JSONObject());
        String nombreProvincia = (String) localizacion.get("level2");
        Provincia provincia = new Provincia(nombreProvincia);

        // Municipio y codigo postal
        String nombreMunicipio = localizacion.get("level5") != null ? ((String) localizacion.get("level5")).trim() : null;
        //String municipio = (String) localizacion.get("upperLevel"); // Demasiado en detalle, describe el barrio
        return new Municipio(nombreMunicipio, provincia);
    }



    private boolean rotarParametros(){

        // Rotamos el tipo de contrato a buscar
        TipoContrato nuevoTipoContrato = TipoContrato.obtenerPorIndice(TipoContrato.indiceDe(tipoContrato) + 1);

        // Hay otro tipo de contrato mas
        if (nuevoTipoContrato != null){
            tipoContrato = nuevoTipoContrato;
            return true;
        }

        // No hay mas contratos para ese tipo de inmueble, cambiaremos de tipo de inmueble
        else {

            // Rotamos el tipo de inmueble a buscar
            TipoInmueble nuevoTipoInmueble = TipoInmueble.obtenerPorIndice(TipoInmueble.indiceDe(tipoInmueble) + 1);

            // Hay otro tipo de inmueble
            if (nuevoTipoInmueble != null){
                tipoInmueble = nuevoTipoInmueble;
                tipoContrato = TipoContrato.obtenerPorIndice(0);
                return true;
            }
        }

        // Hemos terminado de scrapear toodo
        return false;
    }

    private Integer convertirTipoContrato2Int(){
        switch (tipoContrato){

            case COMPRA:
                return 1;

            case ALQUILER:
                return 3;
        }
        return null;
    }

    private String convertirIdTipoAnunciante2Texto(int tipoAnunciante){

        switch (tipoAnunciante){

            case 1:
                return "Particular";

            case 3:
                return "Inmobiliaria";
        }
        return "Desconocido";
    }

    private String convertirIdsTipoInmueble2Texto(int tipoId, int subTipoId){

        // Viviendas
        if (tipoId == 2){

            switch (subTipoId){

                case 1:
                case 52:
                    return "Piso";

                case 2:
                    return "Apartamento";

                case 3:
                    return "Chalet";

                case 5:
                    return "Casa adosada";

                case 6:
                    return "Atico";

                case 7:
                    return "Duplex";

                case 8:
                    return "Loft";

                case 9:
                    return "Finca Rustica";

                case 54:
                    return "Estudio";
            }
        }

        return "Desconocido";
    }

    private String convertirTipoOrientacion2Texto(int orientacion){

        switch (orientacion){

            case 1:
                return "Norte";

            case 2:
                return "Noroeste";

            case 3:
                return "Noreste";

            case 4:
                return "Sur";

            case 5:
                return "Sureste";

            case 6:
                return "Suroeste";

            case 7:
                return "Este";

            case 8:
                return "Oeste";

            default:
                return null;
        }
    }

    private Integer convertirIdOrientacion(int orientacion){

        switch (orientacion){

            case 1:
                return 1;

            case 2:
                return 8;

            case 3:
                return 2;

            case 4:
                return 5;

            case 5:
                return 4;

            case 6:
                return 6;

            case 7:
                return 3;

            case 8:
                return 7;

            default:
                return null;
        }

    }


    @Override
    public void detener() {

        // Terminamos el proxy
        browserMobProxy.stop();

        // Cerramos el chrome driver
        chromeDriver.close();

    }
}
