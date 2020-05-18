$(document).on('change', '#fileInput', function(event) {
    var reader = new FileReader();
  
    reader.onload = function(event) {
        var jsonObj = JSON.parse(event.target.result);

        // Eliminamos el input
        $("#fileInput").remove();

        console.log("Vamos a generar un nuevo informe")

        // Generamos el informe a partir del json
        generarInforme(jsonObj);
    }

    reader.readAsText(event.target.files[0]);
  });

function generarInforme(json){

    let main = $("div.A4");

    let div = $("<div/>", {
        css: {
            backgroundColor: "red"
        }
    });

    let span = $("<span/>");
    span.text(Object.keys(json)[0]);

    div.append(span);
    main.append(div);

    // Añadimos el div final
    let final = $("<div/>", {
        id: "completo"
    });
    div.append(final);

}

function dibujarGrafica(){
    var ctx = $("#myChart")[0].getContext('2d');
    var myChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: ["Green", "Blue", "Gray", "Purple", "Yellow"],
            datasets: [{
            backgroundColor: [
                "#2ecc71",
                "#3498db",
                "#95a5a6",
                "#9b59b6",
                "#f1c40f"
            ],
            data: [12, 19, 3, 17, 28]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                labels: {
                    render: 'percentage',
                    precision: 2,
                    fontColor: function (data) {
                        var rgb = hexToRgb(data.dataset.backgroundColor[data.index]);
                        var threshold = 140;
                        var luminance = 0.299 * rgb.r + 0.587 * rgb.g + 0.114 * rgb.b;
                        return luminance > threshold ? 'black' : 'white';
                    }
                }
            }
        }
    });
}

function hexToRgb(hex) {
    var bigint = parseInt(hex, 16);
    var r = (bigint >> 16) & 255;
    var g = (bigint >> 8) & 255;
    var b = bigint & 255;

    return r + "," + g + "," + b;
}