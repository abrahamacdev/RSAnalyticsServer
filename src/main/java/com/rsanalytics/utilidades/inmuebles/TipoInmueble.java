package com.rsanalytics.utilidades.inmuebles;

public enum  TipoInmueble {
    VIVIENDA(1);

    public final int id;
    private TipoInmueble(int id){
        this.id = id;
    }

    public static TipoInmueble obtenerPorIndice(int indice){

        TipoInmueble[] tipos = TipoInmueble.values();

        if (tipos.length > indice){
            return tipos[indice];
        }

        return null;
    }

    public static TipoInmueble obtenerPorId(int id){
        for (TipoInmueble tipoInmueble : TipoInmueble.values()){
            if (tipoInmueble.id == id){
                return tipoInmueble;
            }
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
