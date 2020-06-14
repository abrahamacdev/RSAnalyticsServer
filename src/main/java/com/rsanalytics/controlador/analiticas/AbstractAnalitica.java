package com.rsanalytics.controlador.analiticas;

import io.reactivex.rxjava3.core.Observable;
import com.rsanalytics.modelo.pojo.scrapers.Inmueble;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.rsanalytics.utilidades.Par;
import com.rsanalytics.utilidades.Utils;
import com.rsanalytics.utilidades.inmuebles.TipoInmueble;
import com.rsanalytics.utilidades.scrapers.TipoContrato;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractAnalitica {

    protected Observable<Par<Inmueble, Map<String, Object>>> observableInmuebles;
    protected JSONObject jsonFinal;
    protected JSONArray jsonAnaliticas;
    protected TipoInmueble tipoInmueble;
    protected TipoContrato tipoContrato;

    public AbstractAnalitica(Observable<Par<Inmueble, Map<String, Object>>> observableInmuebles, TipoInmueble tipoInmueble, TipoContrato tipoContrato){
        this.observableInmuebles = observableInmuebles;
        this.jsonFinal = new JSONObject();
        this.jsonAnaliticas = new JSONArray();
        this.tipoInmueble = tipoInmueble;
        this.tipoContrato = tipoContrato;
    }

    protected List<Method> obtenerMetodos(String tipoMetodos){

        ArrayList<Method> metodos = new ArrayList<>();

        List<Class> classes = Utils.obtenerSuperclasesDe(this.getClass());
        classes.add(this.getClass());

        classes.forEach(clase -> metodos.addAll(Arrays.asList(clase.getDeclaredMethods())));

        return metodos.stream()
                .filter(method -> {
                    String tipo = method.getGenericReturnType().getTypeName();

                    return tipo.equals(tipoMetodos);
                })
                .collect(Collectors.toList());
    }

    public void generarAnalitica(){}

    public JSONObject getJsonFinal(){
        jsonFinal.put("analiticas", jsonAnaliticas);
        jsonFinal.put("idTipoInmueble", tipoInmueble.id);
        jsonFinal.put("idTipoContrato", tipoContrato.id);
        return this.jsonFinal;
    }
}
