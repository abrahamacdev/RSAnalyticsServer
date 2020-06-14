package com.rsanalytics.controlador.refinador;

import com.rsanalytics.controlador.F1;
import com.rsanalytics.controlador.managers.anuncios.ControladorAnuncio;
import com.rsanalytics.controlador.managers.anuncios.ControladorAtributoAnuncio;
import com.rsanalytics.controlador.managers.inmuebles.*;
import com.rsanalytics.modelo.pojo.scrapers.*;
import com.rsanalytics.modelo.pojo.scrapers.anuncio_inmueble_tipoContrato.AnuncioInmuebleTipoContrato;
import com.rsanalytics.modelo.pojo.scrapers.atributo_anuncio.AtributoAnuncio;
import com.rsanalytics.modelo.pojo.scrapers.atributo_inmueble.AtributoInmueble;
import org.tinylog.Logger;
import com.rsanalytics.utilidades.Constantes;
import com.rsanalytics.utilidades.Par;
import com.rsanalytics.utilidades.Utils;
import com.rsanalytics.utilidades.scrapers.ScrapersUtils;
import com.rsanalytics.utilidades.scrapers.TipoContrato;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class Refinador {

    private ExecutorService piscinaHilosRefinador;

    private ControladorAnuncio controladorAnuncio;
    private ControladorAtributoAnuncio controladorAtributoAnuncio;

    private ControladorTipoContrato controladorTipoContrato;
    private ControladorTipoInmueble controladorTipoInmueble;
    private ControladorInmueble controladorInmueble;
    private ControladorAtributoInmueble controladorAtributoInmueble;
    private ControladorAnuncioInmuebleTipoContrato controladorAnuncioInmuebleTipoContrato;

    private ClaveAtributoAnuncio claveTipoContrato;
    private F1 f1;

    public Refinador(ExecutorService piscinaHilosRefinador){
        this.piscinaHilosRefinador = piscinaHilosRefinador;
        this.controladorAnuncio = new ControladorAnuncio();
        this.controladorAtributoAnuncio = new ControladorAtributoAnuncio();
        this.controladorTipoContrato = new ControladorTipoContrato();
        this.controladorTipoInmueble = new ControladorTipoInmueble();
        this.controladorInmueble = new ControladorInmueble();
        this.controladorAtributoInmueble = new ControladorAtributoInmueble();
        this.controladorAnuncioInmuebleTipoContrato = new ControladorAnuncioInmuebleTipoContrato();
        this.f1 = new F1();
        this.claveTipoContrato = controladorAtributoAnuncio.obtenerClaveConNombre("Tipo Contrato");
    }

    public void comenzar(){

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                while (true){
                    try {

                        Par<Exception, List<Integer>> resBusMunAnunsPorRef = controladorAnuncio.obtenerIdsMunicipiosAnunciosPorRefinar();

                        // No hemos podido obtener los ids de los anuncios que hay para refinar
                        if (resBusMunAnunsPorRef.getPrimero() != null){
                            Logger.error(resBusMunAnunsPorRef.getPrimero(), "Ocurrio un error al refinar los datos");
                        }

                        // Obtenemos la lista con los ids de los distintos municipios de los anuncios que hay por refinar
                        List<Integer> idsMunAnunsPorRefinar = resBusMunAnunsPorRef.getSegundo();

                        // Listado de anuncios que formaran cada inmueble
                        List<List<Anuncio>> anunciosParaFormarInmueble = new ArrayList<>(idsMunAnunsPorRefinar.size());

                        // Recorremos los distintos municipios y mezclamos todos los datos
                        for (int idMunicipio : idsMunAnunsPorRefinar){

                            List<List<Anuncio>> temp = cotejarDatosDelMunicipio(idMunicipio);

                            if (temp != null){
                                anunciosParaFormarInmueble.addAll(temp);
                            }
                        }



                        // Obtenemos el listado de inmuebles que guardaremos en la base de datos
                        List<Par<Inmueble, List<AnuncioInmuebleTipoContrato>>> inmueblesParaGuardar = convertirTodosAnunciosEnInmueble(anunciosParaFormarInmueble);

                        // Guardamos la lista de inmuebles
                        if (inmueblesParaGuardar != null) {
                            guardarInmuebles(inmueblesParaGuardar);
                        }

                    }catch (Exception e){
                        Logger.error(e, "Ocurrio un error al refinar los datos");
                    }



                    Logger.info("Hemos terminado de refinar los anuncios disponibles, dormiremos " + Constantes.ESPERA_ENTRE_REFINAMIENTOS.getPrimero() + " " + Constantes.ESPERA_ENTRE_REFINAMIENTOS.getSegundo().name().toLowerCase());

                    // Esperamos dos horas antes de volver a refinar
                    Utils.esperar(Constantes.ESPERA_ENTRE_REFINAMIENTOS.getPrimero(), Constantes.ESPERA_ENTRE_REFINAMIENTOS.getSegundo());
                }
            }
        };

        piscinaHilosRefinador.submit(runnable);
    }

    private List<List<Anuncio>> cotejarDatosDelMunicipio(int idMunicipio){

        Par<Exception, List<Anuncio>> resBusAnuns = controladorAnuncio.obtenerAnuncioPorRefinarConMunicipio(idMunicipio);

        // Ocurrio un error al obtener los datos
        if (resBusAnuns.getPrimero() != null){
            return null;
        }

        // Lista de anuncios de un municipio en concreto
        List<Anuncio> anunciosDelMunicipio = resBusAnuns.getSegundo();

        // Separamos los anuncios anteriores segun su tipo de contrato
        HashMap<String, List<Anuncio>> anunciosSegunContrato = new HashMap<>(TipoContrato.values().length);
        for (TipoContrato tipoContrato : TipoContrato.values()){
            anunciosSegunContrato.put(Utils.capitalize(tipoContrato.name().toLowerCase()), new ArrayList<>());
        }
        for (Anuncio anuncio : anunciosDelMunicipio){

            // Recorremos los anuncios y buscamos una "ClaveAtributoAnuncio" con valor "Tipo Contrato"
            Optional<String> claveAtributoAnuncio = anuncio.getAtributos()
                    .stream()
                    .map(atributoAnuncio -> {
                        return atributoAnuncio.getClaveAtributoAnuncio().equals(claveTipoContrato) ? atributoAnuncio.getValorCadena() : null;
                    })
                    .filter(contrato -> contrato != null)
                    .findFirst();

            if (claveAtributoAnuncio.isPresent()){
                anunciosSegunContrato.get(claveAtributoAnuncio.get()).add(anuncio);
            }
        }

        List<List<Anuncio>> listadoInmuebles = new ArrayList<>();

        // Cogemos todos los anuncios de un municipio-contrato concretos y los cotejamos para obtener
        // la lista de inmuebles finales
        for (String tipoContrato : anunciosSegunContrato.keySet()){
            listadoInmuebles.addAll(cotejarAnuncios(anunciosSegunContrato.get(tipoContrato)));
        }

        return listadoInmuebles;
    }

    private List<List<Anuncio>> cotejarAnuncios(List<Anuncio> posiblesInmuebles){

        // Evitaremos tener que recorrer la lista al completo para aquellos casos que ya
        // hallan sido ligados a otros anuncios
        HashSet<Anuncio> anunciosCogidos = new HashSet<>();
        List<List<Anuncio>> anunciosLigados = new ArrayList<>();

        for (int i=0; i<posiblesInmuebles.size(); i++){

            Anuncio anuncio1 = posiblesInmuebles.get(i);

            // Comprobamos no estar ya ligado a algun otro anuncio
            if (!anunciosCogidos.contains(anuncio1)){

                anunciosCogidos.add(anuncio1);
                List<Anuncio> tempAnunciosLigados = new ArrayList<>(Arrays.asList(anuncio1));

                for (int j=0; j<posiblesInmuebles.size(); j++){

                    Anuncio anuncio2 = posiblesInmuebles.get(j);

                    // Comprobamos que el anuncio con el que nnos vamos a comparar no este
                    // ya ligado a otro
                    if (!anunciosCogidos.contains(anuncio2)){

                        // Comprobamos si los anuncios son iguales a partir del algoritmo desarrollado
                        boolean iguales = false;
                        iguales = comprobarIgualdadAnuncios(anuncio1, anuncio2);

                        if (iguales){
                            anunciosCogidos.add(anuncio2);
                            tempAnunciosLigados.add(anuncio2);
                        }

                    }
                }

                // Guardamos los anuncios que son iguales
                anunciosLigados.add(tempAnunciosLigados);
            }

        }

        return anunciosLigados;
    }

    private boolean comprobarIgualdadAnuncios(Anuncio anuncio1, Anuncio anuncio2){

        Map<String, Object> clavesAnuncio1 = anuncio1.getAtributos()
                .stream()
                .filter(atributoAnuncio -> atributoAnuncio.getClaveAtributoAnuncio().isEsPrincipal())
                .collect(Collectors.toMap(
                        (atributoAnuncio) -> atributoAnuncio.getClaveAtributoAnuncio().getNombre(),
                        (atributoAnuncio) -> atributoAnuncio.getValorActivo()
                ));

        Map<String, Object> clavesAnuncio2 = anuncio2.getAtributos()
                .stream()
                .filter(atributoAnuncio -> atributoAnuncio.getClaveAtributoAnuncio().isEsPrincipal())
                .collect(Collectors.toMap(
                        (atributoAnuncio) -> atributoAnuncio.getClaveAtributoAnuncio().getNombre(),
                        (atributoAnuncio) -> atributoAnuncio.getValorActivo()
                ));

        List<String> extrasAnuncio1 = anuncio1.getAtributos()
                .stream()
                .filter(atributoAnuncio -> !atributoAnuncio.getClaveAtributoAnuncio().isEsPrincipal())
                .map(atributoAnuncio -> atributoAnuncio.getClaveAtributoAnuncio().getNombre())
                .collect(Collectors.toList());

        List<String> extrasAnuncio2 = anuncio2.getAtributos()
                .stream()
                .filter(atributoAnuncio -> !atributoAnuncio.getClaveAtributoAnuncio().isEsPrincipal())
                .map(atributoAnuncio -> atributoAnuncio.getClaveAtributoAnuncio().getNombre())
                .collect(Collectors.toList());

        Par<Integer,Integer> tipoInmueble1 = null;
        if (clavesAnuncio1.containsKey("Id Tipo Inmueble") && clavesAnuncio1.containsKey("Id Subtipo Inmueble")){
            tipoInmueble1 =  new Par(((Double) clavesAnuncio1.get("Id Tipo Inmueble")).intValue(), ((Double) clavesAnuncio1.get("Id Subtipo Inmueble")).intValue());
        }

        Par<Integer,Integer> tipoInmueble2 = null;
        if (clavesAnuncio2.containsKey("Id Tipo Inmueble") && clavesAnuncio2.containsKey("Id Subtipo Inmueble")){
            tipoInmueble2 = new Par(((Double) clavesAnuncio2.get("Id Tipo Inmueble")).intValue(), ((Double) clavesAnuncio2.get("Id Subtipo Inmueble")).intValue());
        }

        // No hemos podido comprobar si son el mismo tipo de inmuebles
        if (tipoInmueble1 == null || tipoInmueble2 == null){
            return false;
        }


        // Comprobamos si los dos inmuebles son del mismo tipo
        if (!ScrapersUtils.mismoTipoInmueble(tipoInmueble1.getPrimero(), tipoInmueble2.getPrimero())){
            return false;
        }

        // Comprobamos si los inmuebles son pisos
        boolean sonPisos = tipoInmueble1.getPrimero() == 1 && ScrapersUtils.viviendaEsPiso(tipoInmueble1.getSegundo());

        if (sonPisos){

            String idAnunciante1 = (String) clavesAnuncio1.get("Id Anunciante");
            String idAnunciante2 = (String) clavesAnuncio2.get("Id Anunciante");

            Double tempPlanta1 = Utils.obtenerDelMap(clavesAnuncio1, "Planta", Double.class);
            Long plantaPiso1 = tempPlanta1 != null ? tempPlanta1.longValue():  null;

            Double tempPlanta2 = Utils.obtenerDelMap(clavesAnuncio2, "Planta", Double.class);
            Long plantaPiso2 = tempPlanta2 != null ? tempPlanta2.longValue():  null;

            // Comprobamos si el id de los anunciantes es el mismo
            if (idAnunciante1.equals(idAnunciante2)){

                String idReferenciaAnunciante1 = (String) clavesAnuncio1.get("Referencia Anunciante");
                String idReferenciaAnunciante2 = (String) clavesAnuncio2.get("Referencia Anunciante");

                // La referencia de los anunciantes es la misma
                if (idReferenciaAnunciante1.equals(idReferenciaAnunciante2)){
                    return true;
                }

                // La referencia no coincide
                else {

                    // Los pisos no estan en la misma planta
                    if (plantaPiso1 != plantaPiso2){
                        return false;
                    }

                    // Misma planta
                    else {

                        // Necesitaran tener una igualdad minima del 95% para ser tratados como el mismo anuncio
                        return f1.comprobarIgualdad(clavesAnuncio1, extrasAnuncio1, clavesAnuncio2, extrasAnuncio2) > 0.95;
                    }
                }
            }

            // Diferente anunciante
            else{

                // No estan en la misma planta
                if (plantaPiso1 != plantaPiso2){
                    return false;
                }

                // Misma planta
                else {
                    // Necesitaran tener una puntuacion f1 > 0.65 para ser considerados iguales
                    return f1.comprobarIgualdad(clavesAnuncio1, extrasAnuncio1, clavesAnuncio2, extrasAnuncio2) > 0.65;
                }
            }

        }

        else {

            Par coordenadasAnuncio1 = new Par(clavesAnuncio1.get("Longitud"), clavesAnuncio1.get("Latitud"));
            Par coordenadasAnuncio2 = new Par(clavesAnuncio2.get("Longitud"), clavesAnuncio2.get("Latitud"));

            // Estan en el mismo sitio
            if (coordenadasAnuncio1.equals(coordenadasAnuncio2)){
                // Si dos inmuebles estan en el mismo
                return f1.comprobarIgualdad(clavesAnuncio1, extrasAnuncio1, clavesAnuncio2, extrasAnuncio2) > 0.35;
            }

            // Estan en sitios diferentes
            else {

                String idAnunciante1 = (String) clavesAnuncio1.get("Id Anunciante");
                String idAnunciante2 = (String) clavesAnuncio2.get("Id Anunciante");

                // Comprobamos si el id de los anunciantes es el mismo
                if (idAnunciante1.equals(idAnunciante2)){

                    String idReferenciaAnunciante1 = (String) clavesAnuncio1.get("Referencia Anunciante");
                    String idReferenciaAnunciante2 = (String) clavesAnuncio2.get("Referencia Anunciante");

                    // La referencia de los anunciantes es la misma
                    if (idReferenciaAnunciante1.equals(idReferenciaAnunciante2)){
                        return true;
                    }

                    // La referencia no coincide
                    else {
                        // Tenemos la sospecha de que sea un anuncio duplicado creado por el mismo anunciante
                        return f1.comprobarIgualdad(clavesAnuncio1, extrasAnuncio1, clavesAnuncio2, extrasAnuncio2) > 0.85;
                    }
                }

                // Los anunciantes son diferentes
                else {

                    // Tenemos sospechas de que puedan ser anuncios que hagan referencia al mismo inmueble
                    // pero redactados por diferentes anunciantes
                    return f1.comprobarIgualdad(clavesAnuncio1, extrasAnuncio1, clavesAnuncio2, extrasAnuncio2) > 0.65;
                }
            }
        }
    }



    private List<Par<Inmueble,List<AnuncioInmuebleTipoContrato>>> convertirTodosAnunciosEnInmueble(List<List<Anuncio>> anuncios){

        Par<Exception, List<com.rsanalytics.modelo.pojo.scrapers.TipoContrato>> resBusTiposContratos = controladorTipoContrato.obtenerTiposContratos();
        Par<Exception, List<TipoInmueble>> resBusTiposInmuebles = controladorTipoInmueble.obtenerTiposInmueble();
        Par<Exception, List<ClaveAtributoInmueble>> resBusClavAtIn = controladorAtributoInmueble.obtenerClavesPosibles();
        if (resBusTiposContratos.getPrimero() != null || resBusTiposInmuebles.getPrimero() != null || resBusClavAtIn.getPrimero() != null){
            Logger.error("Ocurrio un error al convertir los anuncios en inmuebles");
            return null;
        }

        // Obtenemos un map con los tipos de contratos de nuestra base de datos
        Map<String, com.rsanalytics.modelo.pojo.scrapers.TipoContrato> mapTiposContratos = resBusTiposContratos.getSegundo()
                                                                                    .stream()
                                                                                    .collect(Collectors.toMap(t -> t.getNombre(), t -> t));

        // Obtenemos un map con los tipos de inmuebles de nuestra base de datos
        Map<Integer, TipoInmueble> mapTiposInmuebles = resBusTiposInmuebles.getSegundo()
                                                            .stream()
                                                            .collect(Collectors.toMap(t -> t.getId(), t -> t));


        // Obtenemos un map con las distintas claves de los atributos de inmuebles
        Map<String, ClaveAtributoInmueble> mapClavesAtsInmuebles = resBusClavAtIn.getSegundo()
                                                                .stream()
                                                                .collect(Collectors.toMap(t->t.getNombre(), t->t));

        return anuncios.stream()
                .map(listadoAnuncios -> convertirAnunciosEnInmueble(listadoAnuncios, mapTiposInmuebles, mapTiposContratos, mapClavesAtsInmuebles))
                .collect(Collectors.toList());
    }

    private Par<Inmueble,List<AnuncioInmuebleTipoContrato>> convertirAnunciosEnInmueble(List<Anuncio> anuncios, Map<Integer, TipoInmueble> mapTiposInmuebles, Map<String, com.rsanalytics.modelo.pojo.scrapers.TipoContrato> mapTiposContratos, Map<String, ClaveAtributoInmueble> mapClavesAtIn){

        Inmueble inmuebleFinal = new Inmueble();
        Anuncio anuncioBase = null;
        long fechaAnuncio = -1;

        // Obtenemos el anuncio con la fecha mas reciente
        for (Anuncio anuncio : anuncios) {
            long tempFecha = -1;

            for (AtributoAnuncio atributoAnuncio :  anuncio.getAtributos()){
                if (atributoAnuncio.getClaveAtributoAnuncio().getNombre().equals("Fecha Publicacion")){
                    tempFecha = ((Double) atributoAnuncio.getValorActivo()).longValue();
                    break;
                }
            }

            // Actualizamos los datos del anuncio mas reciente
            if (tempFecha > fechaAnuncio){
                anuncioBase = anuncio;
                fechaAnuncio = tempFecha;
            }
        }

        // Si no se ha podido obtener la fecha de ninguno de los anuncios, cogeremos uno al azar par
        // usar sus datos
        if (fechaAnuncio == -1){
            anuncioBase = anuncios.get((int) (Math.random() * anuncios.size()));
        }

        // Indexamos los atributos del anuncio por el nombre de la clave
        Map<String, AtributoAnuncio> atributosDelAnuncioBase =
                anuncioBase.getAtributos()
                        .stream()
                        .collect(Collectors.toMap(at -> at.getClaveAtributoAnuncio().getNombre(),
                                                    at -> at));

        // Obtenemos los atributos del inmueble final
        List<AtributoInmueble> atributosDelInmueble = convertirAtributosAnuncio2AtributosInmueble(inmuebleFinal, atributosDelAnuncioBase, mapClavesAtIn);

        // Obtenemos el tipo de inmueble a partir de los atributos del anuncio
        int idTipoInmueble = atributosDelAnuncioBase.containsKey("Id Tipo Inmueble") ? ((Double) atributosDelAnuncioBase.get("Id Tipo Inmueble").getValorActivo()).intValue() : null;
        TipoInmueble tipoInmueble = mapTiposInmuebles.get(idTipoInmueble);

        // Seteamos los campos al inmueble final
        inmuebleFinal.setMunicipio(anuncioBase.getMunicipio());
        inmuebleFinal.setAtributos(new HashSet<>(atributosDelInmueble));
        inmuebleFinal.setTipoInmueble(tipoInmueble);

        // Obtenemos el tipo de contrato del inmueble a partir del anuncio base
        String nombreTipoContrato = atributosDelAnuncioBase.get("Tipo Contrato").getValorCadena();
        com.rsanalytics.modelo.pojo.scrapers.TipoContrato tipoContrato = mapTiposContratos.get(nombreTipoContrato);

        // Creamos el listado de anuncios que estara ligado a la instancia del inmueble
        List<AnuncioInmuebleTipoContrato> listadoAnunciosQueFormanInmueble = ligarAnunciosAlInmueble(inmuebleFinal, anuncios, tipoContrato);

        return new Par<Inmueble, List<AnuncioInmuebleTipoContrato>>(inmuebleFinal, listadoAnunciosQueFormanInmueble);
    }



    private List<AtributoInmueble> convertirAtributosAnuncio2AtributosInmueble(Inmueble inmueble, Map<String, AtributoAnuncio> atributosDelAnuncio, Map<String, ClaveAtributoInmueble> mapClavesAtIn){

        List<AtributoInmueble> atributosInmueble = new ArrayList<>();

        for (String keyAtributoAnuncio : atributosDelAnuncio.keySet()){

            // COmprobamos que exista un atributo para los inmuebles con el mismo nombre
            // que el actual atributo del anuncio
            if (mapClavesAtIn.containsKey(keyAtributoAnuncio)){

                Object valorAt = atributosDelAnuncio.get(keyAtributoAnuncio).getValorActivo();
                AtributoInmueble atributoInmueble = new AtributoInmueble(inmueble, mapClavesAtIn.get(keyAtributoAnuncio));

                if (valorAt instanceof String){
                    atributoInmueble.setValorCadena((String) valorAt);
                }

                else {
                    atributoInmueble.setValorNumerico((Double) valorAt);
                }

                 atributosInmueble.add(atributoInmueble);
            }
        }

        return atributosInmueble;
    }

    private List<AnuncioInmuebleTipoContrato> ligarAnunciosAlInmueble(Inmueble inmueble, List<Anuncio> anuncios, com.rsanalytics.modelo.pojo.scrapers.TipoContrato tipoContrato){

        ArrayList anunciosParaInmueble = new ArrayList(anuncios.size());

        anuncios.forEach(anuncio -> {
            anunciosParaInmueble.add(new AnuncioInmuebleTipoContrato(anuncio, inmueble, tipoContrato));
        });

        return anunciosParaInmueble;
    }



    private void guardarInmuebles(List<Par<Inmueble,List<AnuncioInmuebleTipoContrato>>> datos){

        // Guardamos cada inmueblle junto con
        for (Par<Inmueble, List<AnuncioInmuebleTipoContrato>> parDatos : datos){

            EntityManager entityManager = Utils.crearEntityManager();
            EntityTransaction entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();

            Par<Exception, Inmueble> resGuardadoInmueble = controladorInmueble.guardarInmueble(parDatos.getPrimero(), entityManager);
            boolean hacerRoll = false;
            // Comprobamos que se halla guardado el registro
            if (resGuardadoInmueble.getSegundo() != null){

                int guardados = controladorAnuncioInmuebleTipoContrato.guardarAnunciosLigados(parDatos.getSegundo(), entityManager);

                // Si no se han guardado todos los datos haremos rollback
                if (guardados != parDatos.getSegundo().size()){
                    hacerRoll = true;
                }
            }

            // Haremos rollback
            if (hacerRoll) {
                entityTransaction.rollback();
            } else {
                entityTransaction.commit();
            }

            entityManager.close();
        }
    }

    private void reintentar() {

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                comenzar();
            }
        };

        // Esperaremos 10 segundos antes de volver a relanzar el refinador
        Timer timer = new Timer();
        timer.schedule(timerTask, 10 * 1000);
    }
}
