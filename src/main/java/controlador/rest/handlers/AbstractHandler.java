package controlador.rest.handlers;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

abstract public class AbstractHandler {

    protected Javalin app;
    protected ExecutorService piscina;

    public AbstractHandler(Javalin app, ExecutorService piscina){
        this.app = app;
        this.piscina = piscina;

        registrarHandlers();
    }

    abstract void registrarHandlers();

    /**
     * Ejecutamos la tarea en la piscina de hilos dedicada a atender peticiones, cuando esta
     * halla terminado de ejecutarse mandara una respuesta al cliente
     * @param function
     * @param ctx
     */
    protected void ejecutar(Function<Context, Runnable> function, Context ctx){

        // Añadimos la tarea a la cola de la piscina de hilos
        CompletableFuture completableFuture = CompletableFuture.runAsync(function.apply(ctx), piscina);

        // Indicamos que el resultado de la peticion estara ligado al completable future
        ctx.result(completableFuture);
    }
}
