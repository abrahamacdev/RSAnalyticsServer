package controlador.analiticas;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import modelo.pojo.scrapers.Informe;
import utilidades.Par;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnaliticaVivienda extends AnaliticaBasica {

    public AnaliticaVivienda(Observable<Par<Informe, HashMap<String, Object>>> observableInformes) {
        super(observableInformes);
    }

    @Override
    public void generarAnalitica() {

        String tipoObserver = Observer.class.getCanonicalName();
        String tipoPar = Par.class.getCanonicalName();
        String tipoInforme = Informe.class.getCanonicalName();
        String tipoHashmap = HashMap.class.getCanonicalName();
        String tipoString = String.class.getCanonicalName();
        String tipoObject = Object.class.getCanonicalName();

        String tipoFinal = tipoObserver + "<" + tipoPar + "<" + tipoInforme + ", " + tipoHashmap + "<" + tipoString + ", " +
                tipoObject + ">>>";

        List<Method> metodos = obtenerMetodos(tipoFinal);



    }
}
