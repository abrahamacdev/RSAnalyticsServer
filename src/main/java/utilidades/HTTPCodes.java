package utilidades;

public enum HTTPCodes {

    // 2XX OK
    _200(200),
    _201(201),
    _206(206),

    // 4XX Cliente
    _400(400),
    _401(401),
    _403(403),
    _404(404),

    // 5XX Server
    _500(500),
    _503(503);

    private int codigo;

    HTTPCodes(int codigo){
        this.codigo = codigo;
    }

    public int getCodigo(){
        return this.codigo;
    }
}
