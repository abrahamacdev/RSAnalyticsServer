package controlador.scrapers.scraper.fotocasa;

import com.google.common.base.Charsets;
import controlador.managers.ControladorAtributo;
import controlador.scrapers.TipoScraper;
import controlador.scrapers.scraper.AbstractScraper;
import controlador.scrapers.OnScraperListener;
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
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.tinylog.Logger;
import utilidades.Par;
import utilidades.Utils;
import utilidades.scrapers.TipoContrato;
import utilidades.scrapers.TipoInmueble;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static utilidades.Constantes.DESCANSO_ENTRE_PAGINAS;
import static utilidades.Constantes.DESCANSO_ENTRE_CICLOS;

public class ScraperFotocasa extends AbstractScraper {

    private ChromeDriver chromeDriver;
    private BrowserMobProxy browserMobProxy;
    private Har har;

    private TipoContrato tipoContrato;
    private TipoInmueble tipoInmueble;

    private ControladorAtributo controladorAtributo;
    private Procedencia procedenciaFotocasa = Procedencia.obtenerProcedenciaConNombre("Fotocasa");
    private Map<String, ClaveAtributoAnuncio> clavesPermitidas = new HashMap<>();

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
        controladorAtributo = new ControladorAtributo();
        List<ClaveAtributoAnuncio> clavesAtributosAnuncios = controladorAtributo.obtenerClavesPosibles();
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

        final int[] recuento = new int[]{0};

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Logger.info("Recuento hasta el momento: " + recuento[0]);

                File archivoCaracteristicas = new File("/home/abraham/Documentos/caracteristicas");

                final String[] cadenaFinal = {""};

                try {
                    FileUtils.write(archivoCaracteristicas, cadenaFinal[0], Charsets.UTF_8);
                } catch (IOException e) {
                    System.out.println("No hemos podido guardar el archivo");
                }

                if (recuento[0] > 1000){
                    System.exit(1);
                }

            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 30000, 30000);

        try {

            boolean continuar = true;
            while (continuar){

                // Vamos a la primera pagina segun el tipo de contrato y el tipo de inmueble
                irPrimeraPaginaDe(tipoContrato, tipoInmueble, "España");

                // Obtenemos los datos de cada anuncio individual y lo añadimos a la lista
                List<JSONObject> anunciosEnJson = obtenerTodosDatos();
                recuento[0] += anunciosEnJson.size();

                // Avisamos de la recoleccion de una tanda de anuncios
                onScraperListener.onScraped(parsearJsons2Pojos(anunciosEnJson), TipoScraper.FOTOCASA);

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
                    onScraperListener.onScraped(parsearJsons2Pojos(anunciosEnJson), TipoScraper.FOTOCASA);

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

        long idInmueble = (long) jsonDetalle.get("id");

        String urlDetalleAnuncio = "https://api.fotocasa.es/PropertySearch/Property?culture=es-ES&locale=es-ES&transactionType=" +
                obtenerTipoTransaccionNumerico() + "&periodicityId=0&id=" + idInmueble;


        HttpClient client = HttpClients.custom().build();
        RequestBuilder requestBuilder = RequestBuilder.get().setUri(urlDetalleAnuncio);

        HttpUriRequest request = requestBuilder.build();

        try {
            HttpResponse response = client.execute(request);
            String html = EntityUtils.toString(response.getEntity());

            return ((JSONObject) new JSONParser().parse(html));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JSONObject();
    }

    private List<JSONObject> obtenerTodosDatos(){

        System.out.println("Obteniendo los datos en detalle...");

        List<JSONObject> listadosAnuncios = obtenerListadosAnuncios();
        List<JSONObject> resumenListadosAnuncios = obtenerResumenListadosAnuncios(listadosAnuncios);
        return resumenListadosAnuncios.stream()
                .map(resumen -> obtenerDatosDetalleAnuncio(resumen))
                .collect(Collectors.toList());
    }

    private List<Anuncio> parsearJsons2Pojos(List<JSONObject> jsonAnuncios){

        return jsonAnuncios.stream()
                .map(anuncio -> parsearJson2Pojo(anuncio))
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

        return anuncio;
    }


    private List<AtributoAnuncio> obtenerAtributosPrincipales(Anuncio anuncio, JSONObject jsonAnuncio){

        List<AtributoAnuncio> atributosPrincipales = new ArrayList<>(17);

        switch (tipoInmueble){

            case VIVIENDA:
                atributosPrincipales.addAll(obtenerAtributosVivienda(anuncio, jsonAnuncio));
        }



        return atributosPrincipales;
    }

    private List<AtributoAnuncio> obtenerAtributosExtras(Anuncio anuncio, JSONObject jsonAnuncio){

        ArrayList<AtributoAnuncio> atributosExtras = new ArrayList<>();

        // Vamos a comprobar las diferentes caracteristicas
        JSONArray extras = (JSONArray) jsonAnuncio.getOrDefault("otherFeatures", new JSONArray());
        for (Object extra : extras) {
            JSONObject tempExtra = (JSONObject) extra;
            Set<String> keys = tempExtra.keySet();
            keys.stream()
                    .filter(key -> clavesPermitidas.containsKey(key))
                    .map(key -> clavesPermitidas.get(key))
                    .forEach(claveAtributoAnuncio -> {

                        AtributoAnuncio atributoAnuncio = new AtributoAnuncio(anuncio, claveAtributoAnuncio, 1);
                        atributosExtras.add(atributoAnuncio);
                    });
        }

        return atributosExtras;
    }

    private List<AtributoAnuncio> obtenerAtributosVivienda(Anuncio anuncio, JSONObject jsonAnuncio){

        int caracteristicasEsperadas = 4;
        HashMap<String, Object> tempAtributos = new HashMap<>(caracteristicasEsperadas);

        JSONArray caracteristicas = (JSONArray) jsonAnuncio.getOrDefault("features", new JSONArray());
        int recogidas = 0;

        if (caracteristicas != null){
            // Recorremmos el listado de las caracteristicas en busca de las esperadas
            for (Object json : caracteristicas){
                JSONObject caracteristica = (JSONObject) json;

                // Obtenemos el numero de baños
                if (caracteristica.containsKey("bathrooms")){
                    tempAtributos.put("Banos", ((Long) caracteristica.get("bathrooms")).doubleValue());
                    recogidas++;
                }

                // Obtenemos el numero de habitaciones
                if (caracteristica.containsKey("rooms")){
                    tempAtributos.put("Numero habitaciones", ((Long) caracteristica.get("rooms")).doubleValue());
                    recogidas++;
                }

                // Evitamos seguir recorriendo el JSON a lo tonto
                if (recogidas == caracteristicasEsperadas){
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

    private int obtenerTipoTransaccionNumerico(){
        switch (tipoContrato){

            case COMPRA:
                return 1;

            case ALQUILER:
                return 3;
        }
        return -1;
    }




    @Override
    public void detener() {

        // Terminamos el proxy
        browserMobProxy.stop();

        // Cerramos el chrome driver
        chromeDriver.close();

    }
}
