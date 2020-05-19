package controlador.analiticas;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import modelo.pojo.scrapers.Informe;
import modelo.pojo.scrapers.Inmueble;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilidades.Par;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnaliticaBasica extends AbstractAnalitica{

    private JSONArray analiticas;
    private JSONObject jsonFinal;

    public AnaliticaBasica(Observable<Par<Informe, HashMap<String, Object>>> observableInformes){
        super(observableInformes);
    }

    private Observer<Par<Inmueble,HashMap<String,Object>>> obtenerAnaliticasPrecio(){
        return new Observer<Par<Inmueble, HashMap<String, Object>>>() {

            double precioMax = -1;
            double precioMin = Double.MAX_VALUE;
            double media = 0.0;
            double numPrecios = 1.0;

            @Override
            public void onSubscribe(@NonNull Disposable d) {}

            @Override
            public void onNext(@NonNull Par<Inmueble, HashMap<String, Object>> inmuebleHashMapPar) {

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

                jsonAnaliticas.add(generarResultadoFinal(new double[]{precioMax, precioMin, media}));
            }
        };
    }

    private JSONObject generarResultadoFinal(double[] valores){

        JSONObject respuesta = new JSONObject();

        JSONArray msgs = new JSONArray();
        msgs.add("El precio máximo de los inmuebles analizados fué de $1€");
        msgs.add("El precio mínimo de los inmuebles analizados fué de $2€");
        msgs.add("El precio medio de los inmuebles analizados fué de $3€");

        JSONArray valoresMsgs = new JSONArray();
        valoresMsgs.add(valores[0]);
        valoresMsgs.add(valores[1]);
        valoresMsgs.add(valores[2]);

        JSONObject formato = new JSONObject();
        formato.put("posicion", 1);
        formato.put("mostrado", 2);

        respuesta.put("msgs", msgs);
        respuesta.put("valores", valoresMsgs);
        respuesta.put("formato", formato);

        return respuesta;
    }
}
