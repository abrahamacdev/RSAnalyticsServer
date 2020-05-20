package controlador.analiticas;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import modelo.pojo.scrapers.ClaveAtributoInmueble;
import modelo.pojo.scrapers.Informe;
import modelo.pojo.scrapers.Inmueble;
import modelo.pojo.scrapers.atributo_inmueble.AtributoInmueble;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilidades.Par;
import utilidades.Tripleta;
import utilidades.Utils;
import utilidades.inmuebles.TipoInmueble;
import utilidades.scrapers.TipoContrato;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnaliticaVivienda extends AnaliticaBasica {

    public AnaliticaVivienda(Observable<Par<Inmueble, Map<String, Object>>> observableInmuebles, TipoInmueble tipoInmueble, TipoContrato tipoContrato) {
        super(observableInmuebles, tipoInmueble, tipoContrato);
    }

    @Override
    public void generarAnalitica() {

        String tipoObserver = Observer.class.getCanonicalName();
        String tipoPar = Par.class.getCanonicalName();
        String tipoInmueble = Inmueble.class.getCanonicalName();
        String tipoMap = Map.class.getCanonicalName();
        String tipoString = String.class.getCanonicalName();
        String tipoObject = Object.class.getCanonicalName();

        String tipoFinal = tipoObserver + "<" + tipoPar + "<" + tipoInmueble + ", " + tipoMap + "<" + tipoString + ", " +
                tipoObject + ">>>";

        try {
            List<Method> metodos = obtenerMetodos(tipoFinal);
            for (Method metodo : metodos) {
                metodo.setAccessible(true);
                observableInmuebles.subscribe((Observer<Par<Inmueble, Map<String, Object>>>) metodo.invoke(this));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private Observer<Par<Inmueble, Map<String,Object>>> obtenerAnaliticasExtras(){
        return new Observer<Par<Inmueble, Map<String, Object>>>() {

            HashMap<String, Double> recuentoDeCadaExtra = new HashMap<>();

            Double numExtrasPromedio = 0.0;
            double numInmuebles = 0;

            @Override
            public void onSubscribe(@NonNull Disposable d) { }

            @Override
            public void onNext(@NonNull Par<Inmueble, Map<String, Object>> inmuebleMapPar) {

                Inmueble inmueble = inmuebleMapPar.getPrimero();

                List<AtributoInmueble> extras = inmueble.getAtributos()
                        .stream()
                        .filter(atributoInmueble -> !atributoInmueble.getClaveAtributoInmueble().isEsPrincipal())
                        .collect(Collectors.toList());

                numExtrasPromedio += extras.size();
                numInmuebles++;

                extras.forEach(atributoInmueble -> {

                    ClaveAtributoInmueble claveAtributoInmueble = atributoInmueble.getClaveAtributoInmueble();

                    double recuento = 1.0;

                    if (recuentoDeCadaExtra.containsKey(claveAtributoInmueble.getNombre())){
                        recuento = recuentoDeCadaExtra.get(claveAtributoInmueble.getNombre()) + 1.0;
                    }

                    recuentoDeCadaExtra.put(claveAtributoInmueble.getNombre(), recuento);

                });
            }

            @Override
            public void onError(@NonNull Throwable e) { }

            @Override
            public void onComplete() {

                // Nº promedio de extras de las viviendas
                JSONObject jsonPromedioExtras = generarAnaliticaExtrasPromedio(numInmuebles, numExtrasPromedio);
                jsonAnaliticas.add(jsonPromedioExtras);

                // Extra + y - común
                Tripleta<JSONObject,String,String> tripletaResp = generarAnaliticaExtraMasMenosComun(recuentoDeCadaExtra, recuentoDeCadaExtra.size());
                jsonAnaliticas.add(tripletaResp.getPrimero());

                // Viviendas con ciertos extras
                JSONObject jsonViviendasConCiertosExtras = generarAnaliticaViviendasConCiertosExtras(recuentoDeCadaExtra, tripletaResp.getSegundo(), tripletaResp.getTercero());
                if (jsonViviendasConCiertosExtras != null){
                    jsonAnaliticas.add(jsonViviendasConCiertosExtras);
                }
            }
        };
    }

    private Observer<Par<Inmueble, Map<String,Object>>> obtenerAnaliticasAntiguedad(){
        return new Observer<Par<Inmueble, Map<String, Object>>>() {

            HashMap<Integer, Double> inmueblesPorAntiguedad = new HashMap<>();

            @Override
            public void onSubscribe(@NonNull Disposable d) { }

            @Override
            public void onNext(@NonNull Par<Inmueble, Map<String, Object>> inmuebleMapPar) {

                double antiguedad = (double) inmuebleMapPar.getSegundo().get("Antiguedad");

                if (antiguedad != 0.0){

                    double recuento = 0.0;

                    if (inmueblesPorAntiguedad.containsKey(antiguedad)){
                        recuento = inmueblesPorAntiguedad.get(antiguedad) + 1.0;
                    }

                    inmueblesPorAntiguedad.put((int) antiguedad, recuento);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) { }

            @Override
            public void onComplete() {

                JSONObject analiticaPromedioAntiguedad = generarAnaliticaPromedioAntiguedad(inmueblesPorAntiguedad);
                if (analiticaPromedioAntiguedad != null){
                    jsonAnaliticas.add(analiticaPromedioAntiguedad);
                }

                JSONObject analiticaAntiguedadesMasComunes = generarAnaliticaAntiguedadesMasComunes(inmueblesPorAntiguedad);
                if (analiticaAntiguedadesMasComunes != null){
                    jsonAnaliticas.add(analiticaAntiguedadesMasComunes);
                }

            }
        };
    }

    private Observer<Par<Inmueble, Map<String,Object>>> obtenerAnaliticasBaniosHabsM2(){
        return new Observer<Par<Inmueble, Map<String, Object>>>() {

            HashMap<Integer, Double> recuentoNumBanios = new HashMap<>();
            HashMap<Integer, Double> recuentoNumHabs = new HashMap<>();
            HashMap<Integer, Double> recuentoM2 = new HashMap<>();

            @Override
            public void onSubscribe(@NonNull Disposable d) { }

            @Override
            public void onNext(@NonNull Par<Inmueble, Map<String, Object>> inmuebleMapPar) {

                // Número de habitaciones
                if (inmuebleMapPar.getSegundo().containsKey("Numero Habitaciones")){
                    int numHabs = ((Double) inmuebleMapPar.getSegundo().get("Numero Habitaciones")).intValue();

                    double recuento = 1.0;

                    if (recuentoNumHabs.containsKey(numHabs)){
                        recuento = recuentoNumHabs.get(numHabs) + 1.0;
                    }

                    recuentoNumHabs.put(numHabs, recuento);
                }

                // Número de banios
                if (inmuebleMapPar.getSegundo().containsKey("Banos")){
                    int numBanios = ((Double) inmuebleMapPar.getSegundo().get("Banos")).intValue();

                    double recuento = 1.0;

                    if (recuentoNumBanios.containsKey(numBanios)){
                        recuento = recuentoNumBanios.get(numBanios) + 1.0;
                    }

                    recuentoNumBanios.put(numBanios, recuento);
                }

                // M2
                if (inmuebleMapPar.getSegundo().containsKey("M2")){
                    int m2 = ((Double) inmuebleMapPar.getSegundo().get("M2")).intValue();

                    double recuento = 1.0;

                    if (recuentoM2.containsKey(m2)){
                        recuento = recuentoM2.get(m2) + 1.0;
                    }

                    recuentoM2.put(m2, recuento);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) { }

            @Override
            public void onComplete() {

                List<JSONObject> analiticasHabsBaniosM2 = generarAnaliticaHabsBaniosM2(recuentoNumBanios, recuentoNumHabs, recuentoM2);
                jsonAnaliticas.addAll(analiticasHabsBaniosM2);
            }
        };
    }


    // --- Extras ---
    private JSONObject generarAnaliticaExtrasPromedio(double numInmuebles, double numExtrasPromedio){

        double promedio = numExtrasPromedio / numInmuebles;

        String msgExtrasPromedio = "En promedio, cada vivienda suele tener un total de $1 extras";

        // Creamos el json con los datos de los textos
        JSONObject jsonTexto = new JSONObject();

        JSONArray msgs = new JSONArray();
        msgs.add(msgExtrasPromedio);

        JSONArray valores = new JSONArray();
        valores.add(promedio);

        JSONObject formato = new JSONObject();
        formato.put("posicion", 2);
        formato.put("mostrado", 1);

        jsonTexto.put("msgs", msgs);
        jsonTexto.put("valores", valores);
        jsonTexto.put("formato", formato);

        // JSON Final
        JSONObject res = new JSONObject();
        res.put("texto", jsonTexto);

        return res;
    }

    private Tripleta<JSONObject,String,String> generarAnaliticaExtraMasMenosComun(HashMap<String, Double> recuentoDeCadaExtra, double total){

        String nomMax = null;
        double valorMax = Double.MIN_VALUE;

        String nomMen = null;
        double valorMen = Double.MAX_VALUE;

        for (Map.Entry<String, Double> entry : recuentoDeCadaExtra.entrySet()) {
            if (entry.getValue() > valorMax){
                valorMax = entry.getValue();
                nomMax = entry.getKey();
            }

            else if (entry.getValue() < valorMen){
                valorMen = entry.getValue();
                nomMen = entry.getKey();
            }
        }


        String msgMayor = "El extra que poseen mayoritariamente las viviendas es '$1'";
        String msgMenor = "El extra que menos viviendas suelen tener es '$2'";
        String msgTotal = "En total se han contabilizado $3 extras diferentes";

        // Creamos el json con los datos de los textos
        JSONObject jsonTexto = new JSONObject();

        JSONArray msgs = new JSONArray();
        msgs.add(msgMayor);
        msgs.add(msgMenor);
        msgs.add( msgTotal);


        JSONArray valores = new JSONArray();
        valores.add("\'" + nomMax + "\'");
        valores.add("\'" + nomMen + "\'");
        valores.add("\'" + total + "\'");

        JSONObject formato = new JSONObject();
        formato.put("posicion", 1);
        formato.put("mostrado", 2);

        jsonTexto.put("msgs", msgs);
        jsonTexto.put("valores", valores);
        jsonTexto.put("formato", formato);

        // Creamos el json con los datos de la grafica
        JSONObject jsonGrafica = new JSONObject();

        JSONArray labels = new JSONArray();
        labels.add(nomMax);
        labels.add(nomMen);
        labels.add("Total");

        JSONArray datos = new JSONArray();
        datos.add((int) valorMax);
        datos.add((int) valorMen);
        datos.add((int) total);

        jsonGrafica.put("tipo", "bar");
        jsonGrafica.put("labels", labels);
        jsonGrafica.put("datos", datos);
        jsonGrafica.put("porcentaje", false);
        jsonGrafica.put("precision", 2);


        // JSON Final
        JSONObject res = new JSONObject();
        res.put("texto", jsonTexto);
        res.put("grafica", jsonGrafica);

        return new Tripleta<>(res, nomMax, nomMen);
    }

    private JSONObject generarAnaliticaViviendasConCiertosExtras(HashMap<String, Double> extras, String masComun, String menosComun){

        // Comprobaremos que al menos halla 4 extras en el map
        if (extras.size() > 3){

            Set<String> extrasNoNombrados = extras.keySet();
            extrasNoNombrados.remove(masComun);
            extrasNoNombrados.remove(menosComun);

            int numExtrasACoger = (int) (extrasNoNombrados.size() * 0.7);

            if (numExtrasACoger > 4){
                numExtrasACoger = 4;
            }

            ArrayList<Par<String, Par<String, Double>>> listadoExtras = new ArrayList<>(numExtrasACoger);

            int actual = 1;
            int numVariable = 1;
            for (String key : extrasNoNombrados){

                if (actual == numExtrasACoger){
                    break;
                }

                String msg = "El extra $" + numVariable + " ha aparecido un total de $" + (numVariable + 1) + " veces";

                // Añadimos el numero de veces que ha aparecido en total un cierto extra
                listadoExtras.add(new Par(msg, new Par<>(key, extras.get(key))));

                actual++;
                numVariable += 2;
            }

            // Creamos el json con los datos de los textos
            JSONObject jsonTexto = new JSONObject();

            // (Texto)
            JSONArray msgs = new JSONArray();
            JSONArray valores = new JSONArray();

            // Grafica
            JSONArray labels = new JSONArray();
            JSONArray datos = new JSONArray();

            listadoExtras.forEach(par -> {

                // Guardamos los datos del texto
                msgs.add(par.getPrimero());
                valores.add("\'" + par.getSegundo().getPrimero() + "\'");
                valores.add(par.getSegundo().getSegundo().intValue());

                // Guardamos los datos de la grafica
                labels.add(par.getSegundo().getPrimero());
                datos.add(par.getSegundo().getSegundo().intValue());
            });


            JSONObject formato = new JSONObject();
            formato.put("posicion", 1);
            formato.put("mostrado", 2);

            jsonTexto.put("msgs", msgs);
            jsonTexto.put("valores", valores);
            jsonTexto.put("formato", formato);

            // Creamos el json con los datos de la grafica
            JSONObject jsonGrafica = new JSONObject();

            jsonGrafica.put("tipo", "bar");
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
    // --------------


    // --- Antigüedad ---
    private JSONObject generarAnaliticaPromedioAntiguedad(HashMap<Integer,Double> inmueblesPorAntiguedad){

        int tipoMasComun = Integer.MIN_VALUE;
        double recuento = 0.0;

        for (Map.Entry<Integer, Double> entry : inmueblesPorAntiguedad.entrySet()) {
            if (entry.getValue() > recuento){
                recuento = entry.getValue();
                tipoMasComun = entry.getKey();
            }
        }

        Par<Integer,Integer> edadInmueble = Utils.obtenerAniosAntiguedadInmueble(tipoMasComun);
        String msgPromedio = "En promedio, las viviendas ";
        if (edadInmueble == null){
            return null;
        }
        else if (edadInmueble.getPrimero() == 0 && edadInmueble.getSegundo() == 0){
            msgPromedio += "acaban de ser construidas (son nuevas)";
        }
        else if (edadInmueble.getPrimero() == 100 && edadInmueble.getSegundo() == Integer.MAX_VALUE){
            msgPromedio += "tienen más de 100 años";
        }
        else {
            msgPromedio += "tienen entre " + edadInmueble.getPrimero() + " y " + edadInmueble.getSegundo() + " años";
        }

        // Creamos el json con los datos de los textos
        JSONObject jsonTexto = new JSONObject();

        // (Texto)
        JSONArray msgs = new JSONArray();
        msgs.add(msgPromedio);

        JSONObject formato = new JSONObject();
        formato.put("posicion", 2);
        formato.put("mostrado", 1);

        jsonTexto.put("msgs", msgs);
        jsonTexto.put("formato", formato);


        // JSON Final
        JSONObject res = new JSONObject();
        res.put("texto", jsonTexto);

        return res;

    }

    private JSONObject generarAnaliticaAntiguedadesMasComunes(HashMap<Integer,Double> inmueblesPorAntiguedad){

        // Comproobamos que halla suficientes datos
        if(inmueblesPorAntiguedad.size() > 3){

            double total = inmueblesPorAntiguedad.values().stream().reduce(new Double(0), Double::sum);

            HashMap<Integer, Tripleta<Integer,Double,String>> masComunes = new HashMap();
            for (int i=0; i<3; i++){
                masComunes.put(i, new Tripleta(0, Double.MIN_VALUE, null));
            }

            Function<Par<Integer, Integer>, String> parseoAntiguedad = new Function<Par<Integer, Integer>, String>() {
                @Override
                public String apply(Par<Integer, Integer> integerIntegerPar) {

                    if (integerIntegerPar == null){
                        return null;
                    }
                    else if (integerIntegerPar.getPrimero() == 0 && integerIntegerPar.getSegundo() == 0){
                        return "acaban de ser construidas (son nuevas)";
                    }
                    else if (integerIntegerPar.getPrimero() == 100 && integerIntegerPar.getSegundo() == Integer.MAX_VALUE){
                        return "tienen más de 100 años";
                    }
                    else {
                        return "tienen entre " + integerIntegerPar.getPrimero() + " y " + integerIntegerPar.getSegundo() + " años";
                    }
                }
            };

            // Nos quedamos con las tres antiguedades más comunes
            for (Map.Entry<Integer,Double> entry : inmueblesPorAntiguedad.entrySet()){

                Par edades = Utils.obtenerAniosAntiguedadInmueble(entry.getKey());

                if (masComunes.size() < 3){
                    masComunes.put(masComunes.size(), new Tripleta(entry.getKey(), entry.getValue().intValue(), parseoAntiguedad.apply(edades)));
                }

                else {

                    for (int i=0; i<3; i++){

                        if (masComunes.get(i).getSegundo() < entry.getValue()){
                            masComunes.put(i, new Tripleta(entry.getKey(), entry.getValue(),parseoAntiguedad.apply(edades)));
                            break;
                        }
                    }
                }
            }

            // Creamos el json con los datos de los textos
            JSONObject jsonTexto = new JSONObject();

            // (Texto)
            JSONArray msgs = new JSONArray();
            JSONArray valores = new JSONArray();

            // Grafica
            JSONArray labels = new JSONArray();
            JSONArray datos = new JSONArray();

            Function<Par<Integer, Integer>, String> parseoTituloAntiguedad = new Function<Par<Integer, Integer>, String>() {
                @Override
                public String apply(Par<Integer, Integer> integerIntegerPar) {

                    if (integerIntegerPar == null){
                        return null;
                    }
                    else if (integerIntegerPar.getPrimero() == 0 && integerIntegerPar.getSegundo() == 0){
                        return "Nuevas";
                    }
                    else if (integerIntegerPar.getPrimero() == 100 && integerIntegerPar.getSegundo() == Integer.MAX_VALUE){
                        return "+100 años";
                    }
                    else {
                        return integerIntegerPar.getPrimero() + " - " + integerIntegerPar.getSegundo() + " años";
                    }
                }
            };

            double sumaTresMayores = 0.0;
            int variables = 1;
            for (Map.Entry<Integer, Tripleta<Integer, Double, String>> entry : masComunes.entrySet()) {
                msgs.add("Un $" + variables + "% de las viviendas " + entry.getValue().getTercero());

                double porcentaje = entry.getValue().getSegundo() * 100.0 / total;
                valores.add(porcentaje);

                String label = parseoTituloAntiguedad.apply(Utils.obtenerAniosAntiguedadInmueble(entry.getValue().getPrimero()));
                labels.add(label);
                datos.add(porcentaje);

                sumaTresMayores += porcentaje;
                variables++;
            }
            labels.add("Otros");
            datos.add(100 - sumaTresMayores);


            JSONObject formato = new JSONObject();
            formato.put("posicion", 1);
            formato.put("mostrado", 2);

            jsonTexto.put("msgs", msgs);
            jsonTexto.put("valores", valores);
            jsonTexto.put("formato", formato);

            // Creamos el json con los datos de la grafica
            JSONObject jsonGrafica = new JSONObject();

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
    // ------------------


    // --- Habs, Baños, M2 ---
    private List<JSONObject> generarAnaliticaHabsBaniosM2(HashMap<Integer,Double> recuentoNumBan, HashMap<Integer,Double> recuentoNumHabs, HashMap<Integer,Double> recuentoM2){

        JSONObject analiticaHabs = generarAnaliticaHabs(recuentoNumHabs);
        JSONObject analiticaBanios = generarAnaliticaBanios(recuentoNumBan);
        JSONObject analiticaM2 = generarAnaliticaM2(recuentoM2);

        ArrayList<JSONObject> analiticas = new ArrayList<>(3);

        if (analiticaHabs != null){
            analiticas.add(analiticaHabs);
        }

        if (analiticaBanios != null){
            analiticas.add(analiticaBanios);
        }

        if (analiticaM2 != null){
            analiticas.add(analiticaM2);
        }

        return analiticas;
    }

    private JSONObject generarAnaliticaHabs(HashMap<Integer,Double> recuentoHabs){

        if (recuentoHabs.size() > 0){

            double total = recuentoHabs.values().stream().reduce(new Double(0), Double::sum);

            HashMap<Integer, Par<Integer,Double>> masComunes = new HashMap();
            for (int i=0; i<3; i++){
                masComunes.put(i, new Par<>(0, Double.MIN_VALUE));
            }

            // Nos quedamos con las tres antiguedades más comunes
            for (Map.Entry<Integer,Double> entry : recuentoHabs.entrySet()){

                if (masComunes.size() < 3){
                    masComunes.put(masComunes.size(), new Par(entry.getKey(), entry.getValue()));
                }

                else {

                    for (int i=0; i<3; i++){

                        if (masComunes.get(i).getSegundo() < entry.getValue()){
                            masComunes.put(i, new Par<>(entry.getKey(), entry.getValue()));
                            break;
                        }
                    }
                }
            }

            // Creamos el json con los datos de los textos
            JSONObject jsonTexto = new JSONObject();

            // (Texto)
            JSONArray msgs = new JSONArray();
            JSONArray valores = new JSONArray();

            // Grafica
            JSONArray labels = new JSONArray();
            JSONArray datos = new JSONArray();

            double sumaTresMayores = 0.0;
            int variables = 1;
            DecimalFormat formato2Decimales = new DecimalFormat("#.##");
            for (Map.Entry<Integer, Par<Integer,Double>> entry : masComunes.entrySet()) {
                msgs.add("Un $" + variables + "% de las viviendas tienen " + entry.getValue().getPrimero() + " habitaciones");

                double porcentaje = entry.getValue().getSegundo() * 100.0 / total;
                valores.add(Double.valueOf(formato2Decimales.format(porcentaje).replace(",", ".")));

                String label = entry.getValue().getPrimero() + " habs";
                labels.add(label);
                datos.add(Double.valueOf(formato2Decimales.format(porcentaje).replace(",", ".")));

                sumaTresMayores += porcentaje;
                variables++;
            }

            double porcenOtros = 100.0 - sumaTresMayores;
            if (porcenOtros > 0.0){
                labels.add("Otros");
                datos.add(100 - sumaTresMayores);
            }


            JSONObject formato = new JSONObject();
            formato.put("posicion", 1);
            formato.put("mostrado", 2);

            jsonTexto.put("msgs", msgs);
            jsonTexto.put("valores", valores);
            jsonTexto.put("formato", formato);

            // Creamos el json con los datos de la grafica
            JSONObject jsonGrafica = new JSONObject();

            jsonGrafica.put("tipo", "bar");
            jsonGrafica.put("labels", labels);
            jsonGrafica.put("datos", datos);
            jsonGrafica.put("porcentaje", true);

            // JSON Final
            JSONObject res = new JSONObject();
            res.put("texto", jsonTexto);
            res.put("grafica", jsonGrafica);

            return res;

        }

        return null;
    }

    private JSONObject generarAnaliticaBanios(HashMap<Integer,Double> recuentoBan){

        if (recuentoBan.size() > 0){

            double total = recuentoBan.values().stream().reduce(new Double(0), Double::sum);

            HashMap<Integer, Par<Integer,Double>> masComunes = new HashMap();
            for (int i=0; i<3; i++){
                masComunes.put(i, new Par<>(0, Double.MIN_VALUE));
            }

            // Nos quedamos con las tres antiguedades más comunes
            for (Map.Entry<Integer,Double> entry : recuentoBan.entrySet()){

                if (masComunes.size() < 3){
                    masComunes.put(masComunes.size(), new Par(entry.getKey(), entry.getValue()));
                }

                else {

                    for (int i=0; i<3; i++){
                        if (masComunes.get(i).getSegundo() < entry.getValue()){
                            masComunes.put(i, new Par<>(entry.getKey(), entry.getValue()));
                            break;
                        }
                    }
                }
            }

            // Creamos el json con los datos de los textos
            JSONObject jsonTexto = new JSONObject();

            // (Texto)
            JSONArray msgs = new JSONArray();
            JSONArray valores = new JSONArray();

            // Grafica
            JSONArray labels = new JSONArray();
            JSONArray datos = new JSONArray();

            double sumaTresMayores = 0.0;
            int variables = 1;
            DecimalFormat formato2Decimales = new DecimalFormat("#.##");
            for (Map.Entry<Integer, Par<Integer,Double>> entry : masComunes.entrySet()) {

                if (entry.getValue().getSegundo() > Double.MIN_VALUE){

                    msgs.add("Un $" + variables + "% de las viviendas tienen " + entry.getValue().getPrimero() + " baños");

                    double porcentaje = entry.getValue().getSegundo() * 100.0 / total;
                    valores.add(Double.valueOf(formato2Decimales.format(porcentaje).replace(",", ".")));

                    String label = entry.getValue().getPrimero() + " baños";
                    labels.add(label);
                    datos.add(Double.valueOf(formato2Decimales.format(porcentaje).replace(",",".")));

                    sumaTresMayores += porcentaje;
                    variables++;
                }

            }
            double porcenOtros = 100.0 - sumaTresMayores;
            if (porcenOtros > 0.0){
                labels.add("Otros");
                datos.add(100 - sumaTresMayores);
            }


            JSONObject formato = new JSONObject();
            formato.put("posicion", 1);
            formato.put("mostrado", 2);

            jsonTexto.put("msgs", msgs);
            jsonTexto.put("valores", valores);
            jsonTexto.put("formato", formato);

            // Creamos el json con los datos de la grafica
            JSONObject jsonGrafica = new JSONObject();

            jsonGrafica.put("tipo", "bar");
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

    private JSONObject generarAnaliticaM2(HashMap<Integer,Double> recuentoM2){

        if (recuentoM2.size() > 0){

            double total = recuentoM2.values().stream().reduce(new Double(0), Double::sum);

            HashMap<Integer, Par<Integer,Double>> masComunes = new HashMap();
            for (int i=0; i<3; i++){
                masComunes.put(i, new Par<>(0, Double.MIN_VALUE));
            }

            // Nos quedamos con las tres antiguedades más comunes
            for (Map.Entry<Integer,Double> entry : recuentoM2.entrySet()){

                if (masComunes.size() < 3){
                    masComunes.put(masComunes.size(), new Par(entry.getKey(), entry.getValue()));
                }

                else {

                    for (int i=0; i<3; i++){
                        if (masComunes.get(i).getSegundo() < entry.getValue()){
                            masComunes.put(i, new Par<>(entry.getKey(), entry.getValue()));
                            break;
                        }
                    }
                }
            }

            // Creamos el json con los datos de los textos
            JSONObject jsonTexto = new JSONObject();

            // (Texto)
            JSONArray msgs = new JSONArray();
            JSONArray valores = new JSONArray();

            // Grafica
            JSONArray labels = new JSONArray();
            JSONArray datos = new JSONArray();

            double sumaTresMayores = 0.0;
            int variables = 1;
            DecimalFormat formato2Decimales = new DecimalFormat("#.##");
            for (Map.Entry<Integer, Par<Integer,Double>> entry : masComunes.entrySet()) {
                msgs.add("Un $" + variables + "% de las viviendas tienen " + entry.getValue().getPrimero() + " m2");

                double porcentaje = entry.getValue().getSegundo() * 100.0 / total;
                valores.add(Double.valueOf(formato2Decimales.format(porcentaje).replace(",", ".")));

                String label = entry.getValue().getPrimero() + " m2";
                labels.add(label);
                datos.add(Double.valueOf(formato2Decimales.format(porcentaje).replace(",",".")));

                sumaTresMayores += porcentaje;
                variables++;
            }
            double porcenOtros = 100.0 - sumaTresMayores;
            if (porcenOtros > 0.0){
                labels.add("Otros");
                datos.add(100.0 - sumaTresMayores);
            }


            JSONObject formato = new JSONObject();
            formato.put("posicion", 1);
            formato.put("mostrado", 2);

            jsonTexto.put("msgs", msgs);
            jsonTexto.put("valores", valores);
            jsonTexto.put("formato", formato);

            // Creamos el json con los datos de la grafica
            JSONObject jsonGrafica = new JSONObject();

            jsonGrafica.put("tipo", "bar");
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
    // -----------------------
}
