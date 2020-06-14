package com.rsanalytics.controlador;

import com.rsanalytics.utilidades.Constantes;
import com.rsanalytics.utilidades.Par;
import com.rsanalytics.utilidades.Utils;

import java.util.*;

public class F1 {

    public F1(){}

    public double comprobarIgualdad(Map<String, Object> anuncio1, List<String> extrasAnuncio1, Map<String , Object> anuncio2, List<String> extrasAnuncio2){

        ArrayList<Double> resultados = new ArrayList();

        double calNumImg = compararNumImagenes(anuncio1, anuncio2);
        resultados.add(calNumImg); // Imagenes

        double calPre = compararPrecio(anuncio1, anuncio2);
        resultados.add(calPre); // Precios

        double calOr = compararOrientacion(anuncio1, anuncio2);
        resultados.add(calOr); // Orientacion

        double calCerts = compararCertificadosEnergeticos(anuncio1, anuncio2);
        resultados.add(calCerts); // Certificado Energetico

        double calAnt = compararAntiguedad(anuncio1, anuncio2);
        resultados.add(calAnt); // Antiguedad

        double calTipIn = compararTipoInmueble(anuncio1, anuncio2);
        resultados.add(calTipIn); // Tipo Inmueble

        double calNumBan = compararBanios(anuncio1, anuncio2);
        resultados.add(calNumBan); // Baños

        double calNumHabs = compararHabitaciones(anuncio1, anuncio2);
        resultados.add(calNumHabs); // Habitaciones

        double calM2 = compararM2(anuncio1, anuncio2);
        resultados.add(calM2); // M2

        // De momento lo eliminaremos, no funciona como se esperaba
        //double calCoor = compararCoordenadas(anuncio1, anuncio2);
        //resultados.add(calCoor); // Coordenadas

        double calExtras = compararExtras(extrasAnuncio1, extrasAnuncio2);
        resultados.add(calExtras); // Extras

        double res = resultados.stream().reduce(new Double(0), Double::sum);

        return res;
    }



    private double compararNumImagenes(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        int numImagenes1 = ((Double) anuncio1.get("Numero Imagenes")).intValue();
        int numImagenes2 = ((Double) anuncio2.get("Numero Imagenes")).intValue();

        double puntuacion = numImagenes1 == numImagenes2 ? 1 : 0;

        return puntuacion * Constantes.PESOS_F1.get("Numero Imagenes");
    }

    private double compararPrecio(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        Double precio1 = (Double) anuncio1.get("Precio");
        Double precio2 = (Double) anuncio2.get("Precio");

        double diferenciaPorcentual = 1.0;
        boolean continuar = true;

        if (precio1 == null || precio2 == null){
            continuar = false;
        }

        if (continuar){
            Double max = Math.max(precio1, precio2);
            Double min = Math.min(precio1, precio2);

            try {

                diferenciaPorcentual = (max - min) * 100 / max / 100;

            }catch (Exception e){

            }
        }


        return (1.0 - diferenciaPorcentual) * Constantes.PESOS_F1.get("Precio");

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

        Integer idOrientacion1 = anuncio1.containsKey("Id Orientacion") ? ((Double) anuncio1.get("Id Orientacion")).intValue() : null;
        Integer idOrientacion2 = anuncio2.containsKey("Id Orientacion") ? ((Double) anuncio2.get("Id Orientacion")).intValue() : null;

        double base = 0;
        boolean continuar = true;

        if (idOrientacion1 == null || idOrientacion2 == null){
            base = 1;
            continuar = false;
        }

        if (idOrientacion1 == idOrientacion2){
            base = 1;
            continuar = false;
        }

        if (continuar){
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

        if (consumo1 == null && consumo2 == null){
            base += 0.5;
        }
        else {
            base += consumo1.equals(consumo2) ? 0.5 : 0;
        }

        if (emisiones1 == null && emisiones2 == null){
            base += 0.5;
        }
        else {
            base += emisiones1.equals(emisiones2) ? 0.5 : 0;
        }

        return base * Constantes.PESOS_F1.get("Certificados Energeticos");
    }

    private double compararAntiguedad(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        Long antiguedad1 = anuncio1.containsKey("Antiguedad") ? ((Double) anuncio1.get("Antiguedad")).longValue() : null;
        Long antiguedad2 = anuncio2.containsKey("Antiguedad") ? ((Double) anuncio2.get("Antiguedad")).longValue() : null;

        boolean continuar = true;
        int base = 0;

        if (antiguedad1 == null || antiguedad2 == null){
            base = 1;
            continuar = false;
        }

        if (continuar){
            base = antiguedad1 == antiguedad2 ? 1 : 0;
        }

        return base * Constantes.PESOS_F1.get("Antiguedad");
    }

    private double compararTipoInmueble(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        Integer idTipoInmueble1 = anuncio1.containsKey("Id Tipo Inmueble") ? ((Double) anuncio1.get("Id Tipo Inmueble")).intValue() : null;
        Integer idSubtipoInmueble1 = anuncio1.containsKey("Id Subtipo Inmueble") ? ((Double) anuncio1.get("Id Subtipo Inmueble")).intValue() : null;

        Integer idTipoInmueble2 = anuncio2.containsKey("Id Tipo Inmueble") ? ((Double) anuncio2.get("Id Tipo Inmueble")).intValue() : null;
        Integer idSubtipoInmueble2 = anuncio2.containsKey("Id Subtipo Inmueble") ? ((Double) anuncio2.get("Id Subtipo Inmueble")).intValue() : null;

        boolean continuar = true;
        double base = 0.0;

        if (idTipoInmueble1 == null || idSubtipoInmueble1 == null || idTipoInmueble2 == null || idSubtipoInmueble2 == null){
            continuar = false;
        }

        if (continuar){
            base = idTipoInmueble1 == idTipoInmueble2 ? 0.35 : 0;
            base += idSubtipoInmueble1 == idSubtipoInmueble2 ? 0.65 : 0;
        }


        return base * Constantes.PESOS_F1.get("Tipo Inmueble");
    }

    private double compararBanios(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        Long banios1 = Utils.obtenerDelMap(anuncio1, "Banos", Double.class).longValue();
        Long banios2 = Utils.obtenerDelMap(anuncio2, "Banos", Double.class).longValue();

        double diferenciaPorcentual = 1;
        boolean continuar = true;

        if (banios1 == null || banios2 == null){
            continuar = false;
        }

        if (continuar){
            Long max = Math.max(banios1, banios2);
            Long min = Math.min(banios1, banios2);

            try {

                diferenciaPorcentual = (max - min) * 100.0 / max / 100.0;

            }catch (Exception e){

            }
        }

        return (1 - diferenciaPorcentual) * Constantes.PESOS_F1.get("Bano");
    }

    private double compararHabitaciones(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        Long habitaciones1 = Utils.obtenerDelMap(anuncio1, "Numero Habitaciones", Double.class).longValue();
        Long habitaciones2 = Utils.obtenerDelMap(anuncio2, "Numero Habitaciones", Double.class).longValue();;

        boolean continuar = true;
        double diferenciaPorcentual = 1;

        if (habitaciones1 == null || habitaciones2 == null){
            continuar = false;
        }

        if (continuar){
            Long max = Math.max(habitaciones1, habitaciones2);
            Long min = Math.min(habitaciones1, habitaciones2);

            try {

                diferenciaPorcentual = (max - min) * 100.0 / max / 100.0;

            }catch (Exception e){

            }
        }

        return (1 - diferenciaPorcentual) * Constantes.PESOS_F1.get("Habitaciones");
    }

    private double compararM2(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        Long metrosCuadrados1 = Utils.obtenerDelMap(anuncio1, "M2", Double.class).longValue();
        Long metrosCuadrados2 = Utils.obtenerDelMap(anuncio2, "M2", Double.class).longValue();

        double diferenciaPorcentual = 0;
        boolean continuar = true;

        if (metrosCuadrados1 == null || metrosCuadrados2 == null){
            continuar = false;
        }

        if (continuar){
            Long max = Math.max(metrosCuadrados1, metrosCuadrados2);
            Long min = Math.min(metrosCuadrados1, metrosCuadrados2);

            try {

                diferenciaPorcentual = (max - min) * 100 / max / 100;

            }catch (Exception e){

            }
        }

        return (1.0 - diferenciaPorcentual) * Constantes.PESOS_F1.get("M2");
    }

    private double compararCoordenadas(Map<String, Object> anuncio1, Map<String , Object> anuncio2){

        Double long1 = Utils.obtenerDelMap(anuncio1, "Longitud", Double.class);
        Double lat1 = Utils.obtenerDelMap(anuncio1, "Latitud", Double.class);

        Double long2 = Utils.obtenerDelMap(anuncio2, "Longitud", Double.class);
        Double lat2 = Utils.obtenerDelMap(anuncio2, "Latitud", Double.class);

        Par<Double, Double> coordenadasAnuncio1 = new Par(long1, lat1);
        Par<Double, Double> coordenadasAnuncio2 = new Par(long2, lat2);

        boolean continuar = true;

        if (coordenadasAnuncio1.algoEsNulo() || coordenadasAnuncio2.algoEsNulo()){
            continuar = false;
        }

        double distanciaMax = Constantes.PESOS_F1.get("Corte Coordenadas");
        double distanciaReal = distanciaMax + 1;

        if (continuar){
            distanciaReal = Utils.distancia(coordenadasAnuncio1.getPrimero(), coordenadasAnuncio1.getSegundo(), coordenadasAnuncio2.getPrimero(), coordenadasAnuncio2.getSegundo());
        }

        double normalizadoInv = Utils.normalizarInv(distanciaReal, 0, distanciaMax);


        return normalizadoInv * Constantes.PESOS_F1.get("Coordenadas");
    }

    private double compararExtras(List<String> extrasAnuncio1, List<String> extrasAnuncio2){

        double porcentajeCoincidentes = 1.0;
        boolean continuar = true;

        if (extrasAnuncio1.size() == 0){
            continuar = false;
            if (extrasAnuncio2.size() > 0){
                porcentajeCoincidentes = 0;
            }
        }

        if (continuar){

            double coincidentes = 0.0;
            HashSet<String> setExtrasMayor = new HashSet<>(extrasAnuncio1.size() > extrasAnuncio2.size() ? extrasAnuncio1 : extrasAnuncio2);
            List<String> iterador = extrasAnuncio1.size() > extrasAnuncio2.size() ? extrasAnuncio2 : extrasAnuncio1;

            for (String s : iterador) {
                if (setExtrasMayor.contains(s)){
                    coincidentes += 1.0;
                }
            }

            porcentajeCoincidentes = (coincidentes * 100.0) / setExtrasMayor.size() / 100.0;
        }


        return porcentajeCoincidentes * Constantes.PESOS_F1.get("Extras");
    }
}
