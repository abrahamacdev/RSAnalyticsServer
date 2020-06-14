package refinador;

import com.rsanalytics.controlador.managers.anuncios.ControladorAnuncio;
import com.rsanalytics.controlador.managers.anuncios.ControladorAtributoAnuncio;
import com.rsanalytics.controlador.refinador.Refinador;
import com.rsanalytics.controlador.scrapers.ManejadorScrapers;
import com.rsanalytics.controlador.scrapers.OnScraperListener;
import com.rsanalytics.controlador.scrapers.TipoScraper;
import com.rsanalytics.modelo.pojo.scrapers.Anuncio;
import com.rsanalytics.modelo.pojo.scrapers.ClaveAtributoAnuncio;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.junit.Assert;
import org.junit.Test;
import org.tinylog.Logger;
import com.rsanalytics.utilidades.Par;
import com.rsanalytics.utilidades.Utils;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TestsRefinador {

    @Test
    public void main(){

        System.setProperty("webdriver.chrome.driver", "/home/abraham/chromedriver");

        Runnable refinadorRunnable = new Runnable() {
            @Override
            public void run() {
                List<Set<Integer>> inmuebles = lanzarRefinador();

                HashSet<HashSet<Integer>> combinaciones = new HashSet<>();

                combinaciones.add(new HashSet<>(Arrays.asList(155034660, 155508034)));
                combinaciones.add(new HashSet<>(Arrays.asList(155378296, 155232931)));
                combinaciones.add(new HashSet<>(Arrays.asList(152875575, 154758901)));
                combinaciones.add(new HashSet<>(Arrays.asList(153270534)));
                combinaciones.add(new HashSet<>(Arrays.asList(153819750)));
                combinaciones.add(new HashSet<>(Arrays.asList(154271348)));
                combinaciones.add(new HashSet<>(Arrays.asList(153818657)));
                combinaciones.add(new HashSet<>(Arrays.asList(153818618)));
                combinaciones.add(new HashSet<>(Arrays.asList(154886272, 153269556)));
                combinaciones.add(new HashSet<>(Arrays.asList(148544613, 155507074)));
                combinaciones.add(new HashSet<>(Arrays.asList(154777309, 155007908)));


                List<Set<Integer>> errores = new ArrayList<>();
                for (Set<Integer> inmueble : inmuebles) {
                    if (!combinaciones.contains(inmueble)){
                        errores.add(inmueble);
                    }
                }
                System.out.println(errores);

                Assert.assertEquals(0, errores.size());
            }
        };

        Runnable lanzarScraper = new Runnable() {
            @Override
            public void run() {
                lanzarScraper(refinadorRunnable);
            }
        };

        //obtenerJsonsPrueba();

        reestablecerDB(lanzarScraper);
    }

    private void reestablecerDB(Runnable runnable){

        try {
            String sql = FileUtils.readFileToString(new File(this.getClass().getResource("../sql/Server.sql").getPath()));
            EntityManager entityManager = Utils.crearEntityManager();
            Session session = entityManager.unwrap(Session.class);
            session.doWork(new Work() {
                @Override
                public void execute(Connection connection) throws SQLException {

                    SqlScriptRunner sqlScriptRunner = new SqlScriptRunner(connection, false);
                    sqlScriptRunner.runScript(new StringReader(sql));
                    entityManager.close();

                    runnable.run();
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void obtenerJsonsPrueba(){
        Utils.descargarJsonDeUrlsAnuncioDetalle("/home/abraham/Documentos/Prueba_Politica.txt");
    }

    private void lanzarScraper(Runnable runnable){

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        ManejadorScrapers manejadorScrapers = new ManejadorScrapers(executorService);
        manejadorScrapers.scrap(new OnScraperListener.OnAcabadoListener() {
            @Override
            public void onAcabadoExitoso(TipoScraper tipoScraper) {
                executorService.shutdownNow();
                runnable.run();
            }

            @Override
            public void onAcabadoErroneo(Exception e, TipoScraper tipoScraper) {
                System.out.println("Acabado");
                System.exit(1);
            }
        });

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<Set<Integer>> lanzarRefinador(){

        ControladorAnuncio controladorAnuncio = new ControladorAnuncio();
        ControladorAtributoAnuncio controladorAtributoAnuncio = new ControladorAtributoAnuncio();
        ClaveAtributoAnuncio claveTipoContrato;

        Refinador refinador = new Refinador(null);
        Method cotejar = null;
        try {

            Optional<Method> optCotejar = new ArrayList<Method>(Arrays.asList(Refinador.class.getDeclaredMethods()))
                    .stream()
                    .filter(method -> method.getName().equals("cotejarDatosDelMunicipio"))
                    .map(method -> {
                        method.setAccessible(true);
                        return method;
                    })
                    .findFirst();

            if (!optCotejar.isPresent()){
                System.exit(1);
            }

            cotejar = optCotejar.get();


            Par<Exception, List<Integer>> resBusMunAnunsPorRef = controladorAnuncio.obtenerIdsMunicipiosAnunciosPorRefinar();

            // No hemos podido obtener los ids de los anuncios que hay para refinar
            if (resBusMunAnunsPorRef.getPrimero() != null) {
                Logger.error(resBusMunAnunsPorRef.getPrimero(), "Ocurrio un error al refinar los datos");
                return null;
            }

            // Obtenemos la clave de "Tipo Contrato"
            claveTipoContrato = controladorAtributoAnuncio.obtenerClaveConNombre("Tipo Contrato");

            // Obtenemos la lista con los ids de los distintos municipios de los anuncios que hay por refinar
            List<Integer> idsMunAnunsPorRefinar = resBusMunAnunsPorRef.getSegundo();

            // Listado final de inmuebles a guardar
            // Listado de anuncios que formaran cada inmueble
            List<List<Anuncio>> anunciosParaFormarInmueble = new ArrayList<>(idsMunAnunsPorRefinar.size());

            // Recorremos los distintos municipios y mezclamos todos los datos
            for (int idMunicipio : idsMunAnunsPorRefinar){

                List<List<Anuncio>> temp = (List<List<Anuncio>>) cotejar.invoke(refinador, idMunicipio);

                if (temp != null){
                    anunciosParaFormarInmueble.addAll(temp);
                }
            }


            List<List<Par>> anunciosPareados = anunciosParaFormarInmueble.stream()
                    .map(anuncios -> {
                        return anuncios.stream()
                            .map(anuncio -> {
                                List<Object> atbs = anuncio.getAtributos()
                                        .stream()
                                        .filter(atributoAnuncio -> {
                                            String nombre = atributoAnuncio.getClaveAtributoAnuncio().getNombre();

                                            return nombre.equals("Url Anuncio") || nombre.equals("Id Anuncio");
                                        })
                                        .map(atributoAnuncio -> atributoAnuncio.getValorActivo())
                                        .collect(Collectors.toList());

                                return new Par(atbs.get(0), atbs.get(1));
                            })
                            .collect(Collectors.toList());
                    }).collect(Collectors.toList());

            return anunciosPareados.stream()
                    .map(anuncios -> anuncios
                            .stream()
                            .map(anuncio -> {
                            if (anuncio.getPrimero() instanceof String){
                                    return ((Double)anuncio.getSegundo()).intValue();
                                }
                                return ((Double)anuncio.getPrimero()).intValue();
                            })
                            .collect(Collectors.toSet())
                    ).collect(Collectors.toList());

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("Llega");
        return null;
    }
}
