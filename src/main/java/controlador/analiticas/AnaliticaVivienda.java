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
import utilidades.Utils;
import utilidades.inmuebles.TipoInmueble;
import utilidades.scrapers.TipoContrato;

import java.lang.reflect.Method;
import java.util.*;
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

            String nombreExtraMayor = null;
            double valorExtraMayor = Double.MIN_VALUE;

            String nombreExtraMenor = null;
            double valorExtraMenor = Double.MAX_VALUE;

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

                    if (recuento < valorExtraMenor){
                        nombreExtraMenor = claveAtributoInmueble.getNombre();
                        valorExtraMenor = recuento;
                    }

                    else if (recuento > valorExtraMayor){
                        nombreExtraMayor = claveAtributoInmueble.getNombre();
                        valorExtraMayor = recuento;
                    }

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
                JSONObject jsonExtraMasMenosComun = generarAnaliticaExtraMasMenosComun(nombreExtraMayor, valorExtraMayor, nombreExtraMenor, valorExtraMenor, recuentoDeCadaExtra.size());
                jsonAnaliticas.add(jsonExtraMasMenosComun);

                // Viviendas con ciertos extras
                JSONObject jsonViviendasConCiertosExtras = generarAnaliticaViviendasConCiertosExtras(recuentoDeCadaExtra, nombreExtraMayor, nombreExtraMenor);
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
                jsonAnaliticas.add(analiticaAntiguedadesMasComunes);
            }
        };
    }



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

    private JSONObject generarAnaliticaExtraMasMenosComun(String nomMax, double valorMax, String nomMen, double valorMen, double total){

        String msgMayor = "El extra que poseen mayoritariamente las viviendas es $1";
        String msgMenor = "El extra que menos viviendas suelen tener es $2";
        String msgTotal = "En total se han contabilizado $3 extras diferentes";

        // Creamos el json con los datos de los textos
        JSONObject jsonTexto = new JSONObject();

        JSONArray msgs = new JSONArray();
        msgs.add(msgTotal);
        msgs.add(msgMayor);
        msgs.add(msgMenor);


        JSONArray valores = new JSONArray();
        valores.add(total);
        valores.add(nomMax);
        valores.add(nomMen);

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
        datos.add(valorMax);
        datos.add(valorMen);
        datos.add(total);

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
                valores.add(par.getSegundo().getPrimero());
                valores.add(par.getSegundo().getSegundo());

                // Guardamos los datos de la grafica
                labels.add(par.getSegundo().getPrimero());
                datos.add(par.getSegundo().getSegundo());
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

            

    }
}
