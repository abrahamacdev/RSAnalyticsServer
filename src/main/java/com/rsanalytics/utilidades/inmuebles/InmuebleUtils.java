package com.rsanalytics.utilidades.inmuebles;

public class InmuebleUtils {

    public static boolean idTipoInmuebleValido(Integer idTipoInmueble){
        if (idTipoInmueble == null) return false;
        return idTipoInmueble == 1;
    }

    public static boolean idSubTipoInmuebleValido(Integer idSubTipoInmueble){
        if (idSubTipoInmueble == null) return false;
        return idSubTipoInmueble >= 1 && idSubTipoInmueble <= 7;
    }

    public static boolean idTipoInmuebleEs(Integer idTipoInmueble, TipoInmueble tipoInmueble){
        if (idTipoInmueble == null || tipoInmueble == null) return false;
        return idTipoInmueble == tipoInmueble.id;
    }

    public static String convertirIdTipoInmueble2Texto(int idTipoInmueble){
        if (idTipoInmuebleValido(idTipoInmueble)){

            switch (idTipoInmueble){

                case 1:
                    return "Vivienda";
            }
        }
        return null;
    }

    public static String convertirIdSubtipoInmueble2Texto(int idTipoInmueble, int idSubTipoInmueble){
        if (idTipoInmuebleValido(idTipoInmueble) && idSubTipoInmuebleValido(idSubTipoInmueble)){

            switch (idTipoInmueble){

                // Vivienda
                case 1:
                    return obtenerNombreVivienda(idSubTipoInmueble);
            }


        }
        return null;
    }

    private static String obtenerNombreVivienda(int idSubTipoInmueble){

        switch (idSubTipoInmueble){

            case 1:
                return "Planta Baja";

            case 2:
                return "Planta Intermedia";

            case 3:
                return "Apartamento";

            case 4:
                return "Atico";

            case 5:
                return "Duplex";

            case 6:
                return "Loft";

            case 7:
                return "Estudio";

            case 20:
                return "Finca Rustica";

            case 21:
                return "Chalet";

            case 22:
                return "Casa Adosada";
        }

        return null;
    }

}
