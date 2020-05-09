package utilidades.scrapers;

public class ScrapersUtils {

    public static boolean viviendaEsPiso(int idSubtipo){

        switch (idSubtipo){

            case 3:
            case 5:
            case 9:
                return false;
        }
        return true;
    }

    public static boolean mismoTipoInmueble(int idTipoInmueble1, int idSubtipoInmueble1, int idTipoInmueble2, int idSubtipoInmueble2){
        if (idTipoInmueble1 == idTipoInmueble2){
            return ScrapersUtils.viviendaEsPiso(idSubtipoInmueble1) == ScrapersUtils.viviendaEsPiso(idSubtipoInmueble2);
        }

        return false;
    }

}
