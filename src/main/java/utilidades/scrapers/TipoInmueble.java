package utilidades.scrapers;

public enum  TipoInmueble {
    VIVIENDA;

    public static TipoInmueble obtenerPorIndice(int indice){

        TipoInmueble[] tipos = TipoInmueble.values();

        if (tipos.length > indice){
            return tipos[indice];
        }

        return null;
    }

    public static int indiceDe(TipoInmueble tipoInmueble){

        TipoInmueble[] tipos = TipoInmueble.values();

        for (int i=0; i<tipos.length; i++){
            if (tipos[i] == tipoInmueble){
                return i;
            }
        }
        return -1;
    }
}
