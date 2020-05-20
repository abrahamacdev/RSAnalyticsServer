package controlador.analiticas;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import modelo.pojo.scrapers.Inmueble;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilidades.Par;
import utilidades.inmuebles.TipoInmueble;
import utilidades.scrapers.TipoContrato;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AnaliticaBasica extends AbstractAnalitica{

    public AnaliticaBasica(Observable<Par<Inmueble, Map<String, Object>>> observableInmuebles, TipoInmueble tipoInmueble, TipoContrato tipoContrato) {
        super(observableInmuebles, tipoInmueble, tipoContrato);
    }


    private Observer<Par<Inmueble, Map<String,Object>>> obtenerDatosCabecera(){
        return new Observer<Par<Inmueble, Map<String, Object>>>() {

            int recuentoAnalizados = 0;
            String municipio = null;

            @Override
            public void onSubscribe(@NonNull Disposable d) { }

            @Override
            public void onNext(@NonNull Par<Inmueble, Map<String, Object>> inmuebleMapPar) {

                recuentoAnalizados++;

                if (municipio == null){
                    municipio = inmuebleMapPar.getPrimero().getMunicipio().getNombre();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) { }

            @Override
            public void onComplete() {
                jsonFinal.put("recuento", recuentoAnalizados);
                jsonFinal.put("municipio", municipio);
            }
        };
    }

    private Observer<Par<Inmueble, Map<String,Object>>> obtenerAnaliticasPrecio(){
        return new Observer<Par<Inmueble, Map<String, Object>>>() {

            double precioMax = -1;
            double precioMin = Double.MAX_VALUE;
            double media = 0.0;
            double numPrecios = 1.0;

            @Override
            public void onSubscribe(@NonNull Disposable d) {}

            @Override
            public void onNext(@NonNull Par<Inmueble, Map<String, Object>> inmuebleHashMapPar) {

                double precio = (double) inmuebleHashMapPar.getSegundo().get("Precio");

                if (precio > precioMax){
                    precioMax = precio;
                }

                if (precio < precioMin){
                    precioMin = precio;
                }

                numPrecios += 1.0;
                media += precio;
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

                media = media / numPrecios;

                JSONObject texto = new JSONObject();

                JSONArray msgs = new JSONArray();
                msgs.add("El precio máximo de los inmuebles analizados fué de $1€");
                msgs.add("El precio mínimo de los inmuebles analizados fué de $2€");
                msgs.add("El precio medio de los inmuebles analizados fué de $3€");

                JSONArray valoresMsgs = new JSONArray();
                valoresMsgs.add(precioMax);
                valoresMsgs.add(precioMin);
                valoresMsgs.add(media);

                JSONObject formato = new JSONObject();
                formato.put("posicion", 2);
                formato.put("mostrado", 2);

                texto.put("msgs", msgs);
                texto.put("valores", valoresMsgs);
                texto.put("formato", formato);

                JSONObject respuesta = new JSONObject();
                respuesta.put("texto", texto);

                jsonAnaliticas.add(respuesta);
            }
        };
    }

    private Observer<Par<Inmueble, Map<String,Object>>> obtenerAnaliticasAnunciante(){
        return new Observer<Par<Inmueble, Map<String, Object>>>() {

            HashMap<String, Double> recuentoAnunciante = new HashMap<>();
            double sumaTotal = 0.0;

            double sumaMax = 0;
            String nombreMax = null;

            @Override
            public void onSubscribe(@NonNull Disposable d) {}

            @Override
            public void onNext(@NonNull Par<Inmueble, Map<String, Object>> inmuebleHashMapPar) {

                Map<String, Object> temp = inmuebleHashMapPar.getSegundo();

                String anunciante = temp.containsKey("Nombre Anunciante") ? (String) temp.get("Nombre Anunciante") : null;

                if (recuentoAnunciante.containsKey(anunciante)){
                    double nuevoRecuento = recuentoAnunciante.get(anunciante) + 1.0;
                    recuentoAnunciante.put(anunciante, nuevoRecuento);

                    // Guardamos el anunciante con más inmuebles
                    if (nuevoRecuento > sumaMax){
                        sumaMax = nuevoRecuento;
                        nombreMax = anunciante;
                    }
                }

                else {
                    recuentoAnunciante.put(anunciante, 1.0);

                    // Guardamos el anunciante con más inmuebles
                    if (1.0 > sumaMax){
                        sumaMax = 1.0;
                        nombreMax = anunciante;
                    }
                }

                sumaTotal += 1.0;
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

                // Mayores anunciantes y sus porcentajes
                JSONObject analiticaMayoresAnunciantes = generarAnaliticaMayoresAnunciantes(recuentoAnunciante, sumaTotal);
                if (analiticaMayoresAnunciantes != null){
                    jsonAnaliticas.add(analiticaMayoresAnunciantes);
                }

                // Numero de inmuebles del mayor anunciante
                JSONObject analiticaMayorAnunciante = generarAnaliticaMayorAnunciante(recuentoAnunciante, sumaMax, nombreMax, sumaTotal);
                if (analiticaMayorAnunciante != null){
                    jsonAnaliticas.add(analiticaMayorAnunciante);
                }

                // Nº total de anunciantes en la zona
                JSONObject analiticaAnunciantesZona = generarAnaliticaAnunciantesZona(recuentoAnunciante);
                jsonAnaliticas.add(analiticaAnunciantesZona);
            }
        };
    }



    private JSONObject generarAnaliticaMayoresAnunciantes(HashMap<String,Double> anunciantes, double sumaTotal){

        int numAnunciantesDiferentes = anunciantes.keySet().size();

        if (numAnunciantesDiferentes >= 3){

            HashMap<Integer, String> anunciantesMasNotorios = new HashMap<>();

            for (Map.Entry<String, Double> anunciante : anunciantes.entrySet()){

                if (anunciantesMasNotorios.size() == 3){

                    String nombreAnunciante = anunciante.getKey();
                    double recuentoActual = anunciante.getValue();

                    double recPrimero = anunciantes.get(anunciantesMasNotorios.get(1));
                    double recSegundo = anunciantes.get(anunciantesMasNotorios.get(2));
                    double recTercero = anunciantes.get(anunciantesMasNotorios.get(3));

                    // Supera al primer
                    if (recuentoActual > recPrimero){
                        anunciantesMasNotorios.put(1, nombreAnunciante);
                    }
                    // Supera al segundo
                    else if (recuentoActual > recSegundo){
                        anunciantesMasNotorios.put(2, nombreAnunciante);
                    }
                    // Supera al tercero
                    else if (recuentoActual > recTercero){
                        anunciantesMasNotorios.put(3, nombreAnunciante);
                    }

                }
                else {
                    anunciantesMasNotorios.put(anunciantesMasNotorios.size() + 1, anunciante.getKey());
                }
            }

            HashMap<String, Double> porcenDelAnunciante = new HashMap<>();

            double sumaDeLosAnunciantes = anunciantesMasNotorios.keySet()
                                        .stream()
                                        .map(key -> {
                                            double numAnunAnunciante = anunciantes.get(anunciantesMasNotorios.get(key));

                                            porcenDelAnunciante.put(anunciantesMasNotorios.get(key), numAnunAnunciante * 100 / sumaTotal);

                                            return numAnunAnunciante;
                                        })
                                        .reduce(new Double(0), Double::sum);

            double porcenMayores = sumaDeLosAnunciantes * 100 / sumaTotal;
            String porcenMayoresAnun = "Los 3 anunciantes con mayor cantidad de inmuebles publicados ocupan un $1% del total:";
            String porcenPrimero = "$2 ocupa un $3% del total";
            String porcenSegundo = "$4 ocupa un $5% del total";
            String porcenTercero = "$6 ocupa un $7% del total";


            // Creamos el json con los datos de los textos
            JSONObject jsonTexto = new JSONObject();

            JSONArray msgs = new JSONArray();
            msgs.add(porcenMayoresAnun);
            msgs.add(porcenPrimero);
            msgs.add(porcenSegundo);
            msgs.add(porcenTercero);

            DecimalFormat formato2Decimales = new DecimalFormat("#.##");
            JSONArray valores = new JSONArray();
            valores.add(porcenMayores);
            porcenDelAnunciante.entrySet().forEach(entry -> {
                        valores.addAll(Arrays.asList(entry.getKey(),
                                Double.valueOf(formato2Decimales.format(entry.getValue()).replace(",", "."))));
                    });

            JSONObject formato = new JSONObject();
            formato.put("posicion", 1);
            formato.put("mostrado", 2);

            jsonTexto.put("msgs", msgs);
            jsonTexto.put("valores", valores);
            jsonTexto.put("formato", formato);

            // Creamos el json con los datos de la grafica
            JSONObject jsonGrafica = new JSONObject();

            JSONArray labels = new JSONArray();
            porcenDelAnunciante.entrySet().forEach(entry -> labels.add(entry.getKey()));

            JSONArray datos = new JSONArray();
            porcenDelAnunciante.entrySet().forEach(entry -> datos.add(entry.getValue()));

            double porcenOtros = 100 - porcenMayores;
            if (porcenOtros > 0.0){
                labels.add("Otros");
                datos.add(100 - porcenMayores);
            }


            jsonGrafica.put("tipo", "pie");
            jsonGrafica.put("labels", labels);
            jsonGrafica.put("datos", datos);
            jsonGrafica.put("porcentaje", true);
            jsonGrafica.put("precision", 2);


            // JSON Final
            JSONObject res = new JSONObject();
            res.put("texto", jsonTexto);
            res.put("grafica", jsonGrafica);

            return res;
        }

        return null;
    }

    private JSONObject generarAnaliticaMayorAnunciante(HashMap<String,Double> anunciantes, double max, String nombreMax, double sumTotal){

        if (max > 1){

            String msgMayorAnunciante = "$1 posee un total de $2 inmuebles en la zona";

            // Creamos el json con los datos de los textos
            JSONObject jsonTexto = new JSONObject();

            JSONArray msgs = new JSONArray();
            msgs.add(msgMayorAnunciante);

            JSONArray valores = new JSONArray();
            valores.add(nombreMax);
            valores.add(max);

            JSONObject formato = new JSONObject();
            formato.put("posicion", 1);
            formato.put("mostrado", 2);

            jsonTexto.put("msgs", msgs);
            jsonTexto.put("valores", valores);
            jsonTexto.put("formato", formato);

            // Creamos el json con los datos de la grafica
            JSONObject jsonGrafica = new JSONObject();

            JSONArray labels = new JSONArray();
            labels.add(nombreMax);
            labels.add("Otros");

            JSONArray datos = new JSONArray();
            datos.add(max);
            datos.add(sumTotal - max);

            jsonGrafica.put("tipo", "pie");
            jsonGrafica.put("labels", labels);
            jsonGrafica.put("datos", datos);
            jsonGrafica.put("porcentaje", false);
            jsonGrafica.put("precision", 2);


            // JSON Final
            JSONObject res = new JSONObject();
            res.put("texto", jsonTexto);
            res.put("grafica", jsonGrafica);

            return res;
        }

        return null;
    }

    private JSONObject generarAnaliticaAnunciantesZona(HashMap<String,Double> anunciantes){

        String anunciantesZona = "En total se han contabilizado $1 anunciantes diferentes en la zona analizada";

        // Creamos el json con los datos de los textos
        JSONObject jsonTexto = new JSONObject();

        JSONArray msgs = new JSONArray();
        msgs.add(anunciantesZona);

        JSONArray valores = new JSONArray();
        valores.add(anunciantes.size());

        JSONObject formato = new JSONObject();
        formato.put("posicion", 2);
        formato.put("mostrado", 2);

        jsonTexto.put("msgs", msgs);
        jsonTexto.put("valores", valores);
        jsonTexto.put("formato", formato);


        // JSON Final
        JSONObject res = new JSONObject();
        res.put("texto", jsonTexto);



        return res;

    }
}
