package controlador.analiticas;

import io.reactivex.rxjava3.core.Observable;
import modelo.pojo.scrapers.Informe;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilidades.Par;
import utilidades.Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractAnalitica {

    protected Observable<Par<Informe, HashMap<String, Object>>> observableInformes;
    protected JSONObject jsonFinal;
    protected JSONArray jsonAnaliticas;

    public AbstractAnalitica(Observable<Par<Informe, HashMap<String, Object>>> observableInformes){
        this.observableInformes = observableInformes;
        this.jsonFinal = new JSONObject();
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
        return this.jsonFinal;
    }
}
