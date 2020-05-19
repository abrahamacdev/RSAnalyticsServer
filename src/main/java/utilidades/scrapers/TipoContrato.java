package utilidades.scrapers;


public enum TipoContrato {
    COMPRA(1),
    ALQUILER(2);

    public int id;
    TipoContrato(int id){
        this.id = id;
    }

    public static TipoContrato obtenerPorIndice(int indice){
        TipoContrato[] tipos = TipoContrato.values();

        if (tipos.length > indice){
            return tipos[indice];
        }

        return null;
    }

    public static TipoContrato obtenerPorId(int id){
        for (TipoContrato tipoContrato : TipoContrato.values()){
            if (tipoContrato.id == id){
                return tipoContrato;
            }
        }
        return null;
    }

    public static int indiceDe(TipoContrato tipoContrato){

        TipoContrato[] tipos = TipoContrato.values();

        for (int i=0; i<tipos.length; i++){
            if (tipos[i] == tipoContrato){
                return i;
            }
        }
        return -1;
    }

}
