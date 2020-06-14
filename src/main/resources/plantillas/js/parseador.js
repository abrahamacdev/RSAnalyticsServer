/*$(document).ready(function(){

    const data = {"analiticas":[{"texto":{"msgs":["El precio máximo de los inmuebles analizados fué de $1\u20AC","El precio mínimo de los inmuebles analizados fué de $2\u20AC","El precio medio de los inmuebles analizados fué de $3\u20AC"],"formato":{"posicion":2,"mostrado":2},"valores":[2695000.0,845000.0,1335000.0]}},{"texto":{"msgs":["Los 3 anunciantes con mayor cantidad de inmuebles publicados ocupan un $1% del total:","$2 ocupa un $3% del total","$4 ocupa un $5% del total","$6 ocupa un $7% del total"],"formato":{"posicion":1,"mostrado":2},"valores":[100.0,"CASAS SOLUCIONES GLOBALES S.L.",33.33,"JIMENEZ CERRINEGRO M. CARMEN",33.33,"GRUPO PHLRE 2014, S.L.",33.33]},"grafica":{"tipo":"pie","datos":[33.333333333333336,33.333333333333336,33.333333333333336],"precision":2,"porcentaje":true,"labels":["CASAS SOLUCIONES GLOBALES S.L.","JIMENEZ CERRINEGRO M. CARMEN","GRUPO PHLRE 2014, S.L."]}},{"texto":{"msgs":["En total se han contabilizado $1 anunciantes diferentes en la zona analizada"],"formato":{"posicion":2,"mostrado":2},"valores":[3]}},{"texto":{"msgs":["En promedio, cada vivienda suele tener un total de $1 extras"],"formato":{"posicion":2,"mostrado":1},"valores":[6.666666666666667]}},{"texto":{"msgs":["El extra que poseen mayoritariamente las viviendas es '$1'","El extra que menos viviendas suelen tener es '$2'","En total se han contabilizado $3 extras diferentes"],"formato":{"posicion":1,"mostrado":2},"valores":["'Terraza'","'Calefacción'","'10.0'"]},"grafica":{"tipo":"bar","datos":[3,1,10],"precision":2,"porcentaje":false,"labels":["Terraza","Calefacción","Total"]}},{"texto":{"msgs":["El extra $1 ha aparecido un total de $2 veces","El extra $3 ha aparecido un total de $4 veces","El extra $5 ha aparecido un total de $6 veces"],"formato":{"posicion":1,"mostrado":2},"valores":["'Piscina'",1,"'Trastero'",2,"'Serv. portería'",3]},"grafica":{"tipo":"bar","datos":[1,2,3],"precision":2,"porcentaje":false,"labels":["Piscina","Trastero","Serv. portería"]}},{"texto":{"msgs":["Un $1% de las viviendas tienen 4 habitaciones","Un $2% de las viviendas tienen 5 habitaciones","Un $3% de las viviendas tienen 6 habitaciones"],"formato":{"posicion":1,"mostrado":2},"valores":[33.33,33.33,33.33]},"grafica":{"tipo":"bar","datos":[33.33,33.33,33.33],"porcentaje":true,"labels":["4 habs","5 habs","6 habs"]}},{"texto":{"msgs":["Un $1% de las viviendas tienen 3 baños","Un $2% de las viviendas tienen 4 baños"],"formato":{"posicion":1,"mostrado":2},"valores":[66.67,33.33]},"grafica":{"tipo":"bar","datos":[66.67,33.33],"precision":2,"porcentaje":true,"labels":["3 baños","4 baños"]}},{"texto":{"msgs":["Un $1% de las viviendas tienen 326 m2","Un $2% de las viviendas tienen 270 m2","Un $3% de las viviendas tienen 335 m2"],"formato":{"posicion":1,"mostrado":2},"valores":[33.33,33.33,33.33]},"grafica":{"tipo":"bar","datos":[33.33,33.33,33.33],"precision":2,"porcentaje":true,"labels":["326 m2","270 m2","335 m2"]}}],"idTipoContrato":1,"recuento":3,"municipio":"Madrid Capital","idTipoInmueble":1};
    generarInforme(data);

})*/

$(document).on('change', '#fileInput', function(event) {
    var reader = new FileReader();
    
    reader.onload = function(event) {
        var jsonObj = JSON.parse(event.target.result);

        // Eliminamos el input
        $("#fileInput").remove();

        // Generamos el informe a partir del json
        generarInforme(jsonObj);

    }

    reader.readAsText(event.target.files[0]);
  });

function generarInforme(json){

    let main = $("#main");
    main.append(generarTituloInforme(json['idTipoContrato'], json['idTipoInmueble'], json['recuento'], json['municipio']));

    for(index in json['analiticas']){
        main.append(generarAnaliticaInforme(json['analiticas'][index]))
    }

    // Añadimos el div final
    let final = $("<div/>", {
        id: "completo"
    });

    console.log('Final')

    setTimeout(function(){
        main.append(final);
    }, 500);
}

function generarTituloInforme(idTipoContrato, idTipoInmueble, recuento, municipio){

    let contenedorTitulo = $("<div/>", {
        class: "contenedor-titulo tarjeta"
    });
    
    let titulo = $("<p/>", {
        class: "titulo alineado-vertical"
    });
    titulo.text("Se han analizado un total de " + recuento + " inmuebles en el municipio de \'" + municipio + "\'")

    contenedorTitulo.append(titulo);

    return contenedorTitulo;
}

function generarAnaliticaInforme(analitica){

    let clasesContenedorAnalitica = "analitica";

    // Texto
    let divTexto = null;
    let opcionesContenedorTexto = {
        ancho: "media-anchura",
        alto: "toda-altura",
        clases: "contenedor-texto"
    }

    // Grafica
    let divGrafica = null;
    let posicionGrafica = 2;
    let canvasGrafica = null;
    let clasesGrafica = null;

    // Hay texto que mostrar
    if(analitica['texto'] != undefined){
        const tempTexto = analitica['texto'];
        const formato = tempTexto['formato'];
        
        // Posicion que tendra la grafica con respecto al texto
        posicionGrafica = posicionGraficaConRespectoTexto(formato['posicion']);

        // Posicion del texto
        opcionesContenedorTexto.clases += " " + parsearIntPosicion2Class(formato['posicion']);
    }

    // No hay grafica
    if(analitica['grafica'] == undefined){
        opcionesContenedorTexto.clases += " un-sexto-altura";
        opcionesContenedorTexto.ancho = "extra-anchura"
    }
    // Hay grafica que parsear
    else {
        const tempDatosGraf = analitica['grafica'];
        canvasGrafica = generarGrafica(tempDatosGraf['datos'], tempDatosGraf['labels'], tempDatosGraf['precision'], tempDatosGraf['porcentaje'], tempDatosGraf['tipo'])

        // Establecemos las clases basicas de la grafica
        clasesGrafica = "contenedor-grafica relativo media-anchura";

        // Establecemos la posicion de la grafica
        const clasePosGrafica = parsearIntPosicion2Class(posicionGrafica);
        if(clasePosGrafica != null){
            clasesGrafica += " " + clasePosGrafica;
        }

        clasesContenedorAnalitica += " un-tercio-altura";
    }

    // Generamos el div con el texto a mostrar
    divTexto = generarContenedorTexto(analitica['texto'], opcionesContenedorTexto);

    // Generamos el div con la grafica a mostrar
    divGrafica = generarContenedorGrafica(canvasGrafica, clasesGrafica);

    // Creamos el div que contendrá los datos de la analitica actual
    const divAnalitica = $("<div/>", {
        class: clasesContenedorAnalitica
    });

    // Añadimos el texto y la grafica al div de la analitica
    divAnalitica.append(divTexto);
    divAnalitica.append(divGrafica);

    return divAnalitica;
}

function generarContenedorTexto(texto, opcionesContenedorTexto){

    let clasesFinales = "";

    for(i in opcionesContenedorTexto){
        clasesFinales += opcionesContenedorTexto[i] + " ";
    }

    const contenedorAnalitica = $("<div/>", {
        class: clasesFinales
    })

    // Queremos una lista
    if(texto['formato']['mostrado'] == 2){
        contenedorAnalitica.append(generarLista(texto['msgs'], texto['valores']));
    }

    // Queremos textos seguidos
    else {

        /*const textos = generarTextos(texto['msgs'], texto['valores']);
        
        for(i in textos){
            contenedorAnalitica.append(textos[i]);
        }*/
    }

    return contenedorAnalitica
}

function generarLista(msgs, valores){

    const lista = $("<ul/>", {
        class: "lista"
    });

    for(i in msgs){
        const msg = parsearTexto(msgs[i], valores);

        const fila = $("<li/>");
        fila.html(msg);

        lista.append(fila);
    }

    return lista;
}

function generarContenedorGrafica(canvasGrafica, clasesGrafica){

    if(canvasGrafica != null){
        divGrafica = $("<div/>", {
            class: clasesGrafica
        });
        divGrafica.append(canvasGrafica);
    
        return divGrafica;
    }
}

function generarGrafica(datos, labels, precision, porcentaje, tipoGrafica){

    let colores = [];
    for(i=0; i<datos.length; i++){
        colores.push(generarColorHEX());
    }

    let renderizado = porcentaje ? 'percentage' : 'value'
    let comenzarEnZero = null;
    let mostrarLeyenda = false;
    if(tipoGrafica === 'pie'){
        mostrarLeyenda = true;
    }
    else{

        // Escondemos las lineas de las graficas para el eje "y" y modificamos los valores máximos de este eje
        const valoresSaltos = Math.max(...datos) / 5;
        comenzarEnZero = {
            yAxes: [{
                ticks: {
                    beginAtZero:true,
                    suggestedMax: Math.max(...datos) + valoresSaltos
                },
            }],
            xAxes: [{
                gridLines: {
                    display:false
                }
            }]
        }

        // Pondremos encima de cada "barra" el valor deseado
        if(porcentaje){
            renderizado = function(args){
                return args.value + "%";
            }
        }
    }

    const canvas = $("<canvas/>");
    new Chart(canvas, {
        type: tipoGrafica,
        data: {
            labels: labels,
            datasets: [
                {
                    backgroundColor: colores,
                    data: datos
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            legend: {
                display: mostrarLeyenda
            },
            plugins: {
                labels: {
                    render: renderizado,
                    precision: precision,
                    fontColor: function (opts) {
                        if(tipoGrafica === 'pie'){
                            var rgb = hexToRgb(opts.backgroundColor.substring(1, opts.backgroundColor.length));
                            var threshold = 140;
                            var luminance = 0.299 * rgb.r + 0.587 * rgb.g + 0.114 * rgb.b;
                            return luminance > threshold ? 'black' : 'white';
                        }
                        return 'black';
                    }
                }
            },
            scales: comenzarEnZero
        }
    });

    return canvas;
}



function parsearTexto(msg, valores){

    const patron = /\$[0-9]+/g;

    const matches = msg.match(patron);

    for(i in matches){
        let pos = matches[i].split(/\$/g)[1] - 1;

        if(valores.length > pos){

            let enNegrita = $("<b/>");
            enNegrita.text(valores[pos]);

            msg = msg.replace(matches[i], enNegrita.prop('outerHTML'))
        }
    }

    return msg;
}

function generarLetra(){
	var letras = ["a","b","c","d","e","f","0","1","2","3","4","5","6","7","8","9"];
	var numero = (Math.random()*15).toFixed(0);
	return letras[numero];
}
	
function generarColorHEX(){
	var coolor = "";
	for(var i=0;i<6;i++){
		coolor = coolor + generarLetra() ;
	}
	return "#" + coolor;
}

function posicionGraficaConRespectoTexto(posicion){

    switch(posicion){

        case 1:
            return 3;

        case 2:
            return null;

        case 3:
            return 1;
    }
    return null;
}

function parsearIntPosicion2Class(posicion){

    switch(posicion){
        
        case 1:
            return "a-la-izquierda";

        case 2:
            return "en-el-medio";

        case 3:
            return "a-la-derecha"
    }

    return null;
}

function hexToRgb(hex) {
    var bigint = parseInt(hex, 16);
    var r = (bigint >> 16) & 255;
    var g = (bigint >> 8) & 255;
    var b = bigint & 255;

    return {
        r: r,
        g: g,
        b: b
    }
}
