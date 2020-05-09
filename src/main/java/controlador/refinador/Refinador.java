package controlador.refinador;

import controlador.F1;
import controlador.managers.anuncios.ControladorAnuncio;
import controlador.managers.anuncios.ControladorAtributoAnuncio;
import modelo.pojo.scrapers.Anuncio;
import modelo.pojo.scrapers.ClaveAtributoAnuncio;
import modelo.pojo.scrapers.Inmueble;
import modelo.pojo.scrapers.atributo_anuncio.AtributoAnuncio;
import org.apache.commons.lang3.ObjectUtils;
import org.tinylog.Logger;
import utilidades.Par;
import utilidades.Utils;
import utilidades.scrapers.ScrapersUtils;
import utilidades.scrapers.TipoContrato;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class Refinador {

    private ExecutorService piscinaHilosRefinador;

    private ControladorAnuncio controladorAnuncio;
    private ControladorAtributoAnuncio controladorAtributoAnuncio;

    private ClaveAtributoAnuncio claveTipoContrato;
    private F1 f1;

    public Refinador(ExecutorService piscinaHilosRefinador){
        this.piscinaHilosRefinador = piscinaHilosRefinador;
        this.controladorAnuncio = new ControladorAnuncio();
        this.controladorAtributoAnuncio = new ControladorAtributoAnuncio();
    }

    public void comenzar(){

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Par<Exception, List<Integer>> resBusMunAnunsPorRef = controladorAnuncio.obtenerIdsMunicipiosAnunciosPorRefinar();

                // No hemos podido obtener los ids de los anuncios que hay para refinar
                if (resBusMunAnunsPorRef.getPrimero() != null){
                    Logger.error(resBusMunAnunsPorRef.getPrimero(), "Ocurrio un error al refinar los datos");
                    reintentar();
                    return;
                }

                // Obtenemos la clave de "Tipo Contrato"
                claveTipoContrato = controladorAtributoAnuncio.obtenerClaveConNombre("Tipo Contrato");

                // Obtenemos la lista con los ids de los distintos municipios de los anuncios que hay por refinar
                List<Integer> idsMunAnunsPorRefinar = resBusMunAnunsPorRef.getSegundo();

                // Listado final de inmuebles a guardar
                ArrayList<Inmueble> inmueblesFinales = new ArrayList<>(idsMunAnunsPorRefinar.size());

                // Recorremos los distintos municipios y mezclamos todos los datos
                for (int idMunicipio : idsMunAnunsPorRefinar){

                    List<Inmueble> temp = cotejarDatosDelMunicipio(idMunicipio);

                    if (temp != null){
                        inmueblesFinales.addAll(temp);
                    }
                }
            }
        };

        piscinaHilosRefinador.submit(runnable);
    }

    private List<Inmueble> cotejarDatosDelMunicipio(int idMunicipio){

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

        // Cogemos todos los anuncios de un municipio-contrato concretos y los cotejamos para obtener
        // la lista de inmuebles finales
        for (String tipoContrato : anunciosSegunContrato.keySet()){

        }


        return null;

    }

    private List<Inmueble> cotejarAnuncios(List<Anuncio> posiblesInmuebles){

        ArrayList<Inmueble> inmueblesFinales = new ArrayList<>();

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
                        if (comprobarIgualdadAnuncios(anuncio1, anuncio2)){
                            anunciosCogidos.add(anuncio2);
                            tempAnunciosLigados.add(anuncio2);
                        }

                    }
                }

                // Guardamos los anuncios que son iguales
                anunciosLigados.add(tempAnunciosLigados);

            }
        }


        return inmueblesFinales;
    }

    private boolean comprobarIgualdadAnuncios(Anuncio anuncio1, Anuncio anuncio2){

        Map<String, Object> clavesAnuncio1 = anuncio1.getAtributos()
                .stream()
                .collect(Collectors.toMap(
                        (atributoAnuncio) -> atributoAnuncio.getClaveAtributoAnuncio().getNombre(),
                        (atributoAnuncio) -> atributoAnuncio.getValorActivo()
                ));

        Map<String, Object> clavesAnuncio2 = anuncio2.getAtributos()
                .stream()
                .collect(Collectors.toMap(
                        (atributoAnuncio) -> atributoAnuncio.getClaveAtributoAnuncio().getNombre(),
                        (atributoAnuncio) -> atributoAnuncio.getValorActivo()
                ));

        Par<Integer,Integer> tipoInmueble1 = new Par((Integer) clavesAnuncio1.get("Id Tipo Inmueble"), (Integer) clavesAnuncio1.get("Id Subtipo Inmueble"));
        Par<Integer,Integer> tipoInmueble2 = new Par((Integer) clavesAnuncio2.get("Id Tipo Inmueble"), (Integer) clavesAnuncio2.get("Id Subtipo Inmueble"));

        // Comprobamos si los dos inmuebles son del mismo tipo
        if (!ScrapersUtils.mismoTipoInmueble(tipoInmueble1.getPrimero(), tipoInmueble1.getSegundo(), tipoInmueble2.getPrimero(), tipoInmueble2.getSegundo())){
            return false;
        }

        // Comprobamos si los inmuebles son pisos
        boolean sonPisos = tipoInmueble1.getPrimero() == 2 && ScrapersUtils.viviendaEsPiso(tipoInmueble1.getSegundo());

        if (sonPisos){

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

                    Long plantaPiso1 = (Long) clavesAnuncio1.get("Planta");
                    Long plantaPiso2 = (Long) clavesAnuncio2.get("Planta");

                    // Los pisos no estan en la misma planta
                    if (plantaPiso1 != plantaPiso2){
                        return false;
                    }

                    // Misma planta
                    else {
                        // Necesitaran tener una igualdad minima del 85%
                        return f1.comprobarIgualdad(clavesAnuncio1, clavesAnuncio2) > 0.85;
                    }
                }
            }

            // Diferente anunciante
            else{

                Long plantaPiso1 = (Long) clavesAnuncio1.get("Planta");
                Long plantaPiso2 = (Long) clavesAnuncio2.get("Planta");

                // No estan en la misma planta
                if (plantaPiso1 != plantaPiso2){
                    return false;
                }

                // Misma planta
                else {
                    // Necesitaran tener una puntuacion f1 > 0.7 para ser considerados iguales
                    return f1.comprobarIgualdad(clavesAnuncio1, clavesAnuncio2) > 0.7;
                }
            }

        }

        else {

            Par coordenadasAnuncio1 = new Par(clavesAnuncio1.get("Longitud"), clavesAnuncio1.get("Latitud"));
            Par coordenadasAnuncio2 = new Par(clavesAnuncio2.get("Longitud"), clavesAnuncio2.get("Latitud"));

            // Estan en el mismo sitio
            if (coordenadasAnuncio1.equals(coordenadasAnuncio2)){
                // Si dos inmuebles estan en el mismo
                return f1.comprobarIgualdad(clavesAnuncio1, clavesAnuncio2) > 0.35;
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
                        return f1.comprobarIgualdad(clavesAnuncio1, clavesAnuncio2) > 0.85;
                    }
                }

                // Los anunciantes son diferentes
                else {

                    // Tenemos sospechas de que puedan ser anuncios que hagan referencia al mismo inmueble
                    // pero redactados por diferentes anunciantes
                    return f1.comprobarIgualdad(clavesAnuncio1, clavesAnuncio2) > 0.65;
                }
            }
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
