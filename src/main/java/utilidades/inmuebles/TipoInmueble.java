package utilidades.inmuebles;

import modelo.pojo.rest.Tipo;

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
