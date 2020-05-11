package utilidades;

import org.apache.http.util.Asserts;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.JUnit4;
import utilidades.Constantes;

public class TestsConstantes {

    @Test
    public void comprobarSumaPesosF1Igual1(){

        double resultado = Constantes.PESOS_F1.keySet()
                .stream()
                .filter(key -> !key.startsWith("Corte"))
                .map(key -> Constantes.PESOS_F1.get(key))
                .reduce(new Double(0), Double::sum);

        Assert.assertEquals(1.0, resultado, 0.0);
    }

}
