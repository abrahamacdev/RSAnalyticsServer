package controlador;

import utilidades.Constantes;
import utilidades.Par;
import utilidades.Utils;

import java.util.HashMap;
import java.util.Map;

public class F1 {

    public F1(){}

    public double comprobarIgualdad(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        double puntuacionNumImagenes = compararNumImagenes(anuncio1, anuncio2);


        return -1;
    }



    private double compararNumImagenes(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        int numImagenes1 = (int) anuncio1.get("Numero Imagenes");
        int numImagenes2 = (int) anuncio2.get("Numero Imagenes");

        double puntuacion = numImagenes1 == numImagenes2 ? 1 : 0;

        return puntuacion * Constantes.PESOS_F1.get("Numero Imagenes");
    }

    /**
     * Comparamos cuan iguales son las orientaciones de los dos anuncios en base a la distancia
     * entre ambas.
     * Si son iguales el resultado sera 1, si la diferencia == 1 el resultado sera 0.5, por defecto 0
     * Ejemplo:
     *          Anun1=Norte, Anun2=Norte -> Diferencia = 0 -> Resultado = 1
     *          Anun1=Norte, Anun2=Este -> Diferencia = 2 -> Resultado = 0
     *          Anun1=Norte, Anun2=Noreste -> Diferencia = 1 -> Resultado = 0.5
     * @param anuncio1
     * @param anuncio2
     * @return
     */
    private double compararOrientacion(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        int idOrientacion1 = (int) anuncio1.get("Id Orientacion");
        int idOrientacion2 = (int) anuncio2.get("Id Orientacion");

        double base = 0;

        if (idOrientacion1 == idOrientacion2){
            base = 1;
        }

        else {
            int diferencia = Math.abs(idOrientacion1 - idOrientacion2);

            if (diferencia == 7){
                diferencia = 1;
            }

            base = diferencia == 1 ? 0.5 : 0;
        }

        return base * Constantes.PESOS_F1.get("Orientacion");
    }

    private double compararCertificadosEnergeticos(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        String consumo1 = (String) anuncio1.get("Consumo");
        String emisiones1 = (String) anuncio1.get("Emisiones");

        String consumo2 = (String) anuncio2.get("Consumo");
        String emisiones2 = (String) anuncio2.get("Emisiones");

        double base = 0;

        base += consumo1 == consumo2 ? 0.5 : 0;
        base += emisiones2 == emisiones2 ? 0.5 : 0;

        return base * Constantes.PESOS_F1.get("Certificados Energeticos");
    }

    private double compararAntiguedad(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        Long antiguedad1 = (Long) anuncio1.get("Antiguedad");
        Long antiguedad2 = (Long) anuncio2.get("Antiguedad");

        int base = antiguedad1 == antiguedad2 ? 1 : 0;

        return base * Constantes.PESOS_F1.get("Antiguedad");
    }

    private double compararTipoInmueble(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        int idTipoInmueble1 = (int) anuncio1.get("Id Tipo Inmueble");
        int idSubtipoInmueble1 = (int) anuncio1.get("Id Subtipo Inmueble");

        int idTipoInmueble2 = (int) anuncio2.get("Id Tipo Inmueble");
        int idSubtipoInmueble2 = (int) anuncio2.get("Id Subtipo Inmueble");

        double base = idTipoInmueble1 == idTipoInmueble2 ? 0.35 : 0;
        base += idSubtipoInmueble1 == idSubtipoInmueble2 ? 0.65 : 0;

        return base * Constantes.PESOS_F1.get("Tipo Inmueble");
    }

    private double compararBanios(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        Long banios1 = (Long) anuncio1.get("Banos");
        Long banios2 = (Long) anuncio2.get("Banos");

        if (banios1 == null || banios2 == null){
            return 0;
        }

        Long max = Math.max(banios1, banios2);
        Long min = Math.min(banios1, banios2);

        double diferenciaPorcentual = -1;

        try {

            diferenciaPorcentual = (max - min) * 100 / max / 100;

        }catch (Exception e){

        }

        if (diferenciaPorcentual == -1){
            return 0;
        }

        return (diferenciaPorcentual <= Constantes.PESOS_F1.get("Corte Bano") ? 1 : diferenciaPorcentual)
                * Constantes.PESOS_F1.get("Bano");
    }

    private double compararHabitaciones(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        Long habitaciones1 = (Long) anuncio1.get("Numero Habitaciones");
        Long habitaciones2 = (Long) anuncio2.get("Numero Habitaciones");

        if (habitaciones1 == null || habitaciones2 == null){
            return 0;
        }

        Long max = Math.max(habitaciones1, habitaciones2);
        Long min = Math.min(habitaciones1, habitaciones2);

        double diferenciaPorcentual = -1;

        try {

            diferenciaPorcentual = (max - min) * 100 / max / 100;

        }catch (Exception e){

        }

        if (diferenciaPorcentual == -1){
            return 0;
        }

        return (diferenciaPorcentual <= Constantes.PESOS_F1.get("Corte Habitaciones") ? 1 : diferenciaPorcentual)
                * Constantes.PESOS_F1.get("Bano");
    }

    private double compararM2(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        Long metrosCuadrados1 = (Long) anuncio1.get("M2");
        Long metrosCuadrados2 = (Long) anuncio2.get("M2");

        if (metrosCuadrados1 == null || metrosCuadrados2 == null){
            return 0;
        }

        Long max = Math.max(metrosCuadrados1, metrosCuadrados2);
        Long min = Math.min(metrosCuadrados1, metrosCuadrados2);

        double diferenciaPorcentual = -1;

        try {

            diferenciaPorcentual = (max - min) * 100 / max / 100;

        }catch (Exception e){

        }

        if (diferenciaPorcentual == -1){
            return 0;
        }

        return (diferenciaPorcentual <= Constantes.PESOS_F1.get("Corte M2") ? 1 : diferenciaPorcentual)
                * Constantes.PESOS_F1.get("M2");
    }

    private double compararCoordenadas(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        Par<Double, Double> coordenadasAnuncio1 = new Par(anuncio1.get("Longitud"), anuncio1.get("Latitud"));
        Par<Double, Double> coordenadasAnuncio2 = new Par(anuncio2.get("Longitud"), anuncio2.get("Latitud"));

        if (coordenadasAnuncio1.algoEsNulo() || coordenadasAnuncio2.algoEsNulo()){
            return 0;
        }

        double distanciaReal = Utils.distancia(coordenadasAnuncio1.getPrimero(), coordenadasAnuncio1.getSegundo(), coordenadasAnuncio2.getPrimero(), coordenadasAnuncio2.getSegundo());

        double distanciaMax = Constantes.PESOS_F1.get("Corte Coordenadas");

        if (distanciaReal >= distanciaMax){
            return 0;
        }

        double normalizadoInv = Utils.normalizarInv(distanciaReal, 0, distanciaMax);


        return normalizadoInv * Constantes.PESOS_F1.get("Coordenadas");
    }

}
