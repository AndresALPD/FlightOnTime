function predecir() {

    limpiarErrores();

    const resultadoDiv = document.getElementById("resultado");
    resultadoDiv.innerHTML = "Procesando predicción... ⏳";

    const data = {
        aerolinea: document.getElementById("aerolinea").value,
        origen: document.getElementById("origen").value,
        destino: document.getElementById("destino").value,
        hora_salida: parseInt(document.getElementById("hora_salida").value),
        fecha_salida: document.getElementById("fecha_salida").value,
        distancia_km: parseFloat(document.getElementById("distancia_km").value),
        taxi_out: parseInt(document.getElementById("taxi_out").value)
    };

    function limpiarErrores() {
        document.querySelectorAll(".error").forEach(e => e.innerText = "");
    }


    fetch("http://localhost:8080/api/flight-delay/predict", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => {
                    throw err;
                });
            }
            return response.json();
        })
        .then(result => {
            mostrarResultado(result);
        })
        .catch(error => {
            mostrarError(error);
        });
}

function mostrarResultado(data) {

    const resultadoDiv = document.getElementById("resultado");

    let colorFondo;
    let icono;

    if (data.nivel_riesgo === "ALTO") {
        colorFondo = "#f8d7da";
        icono = "🚨";
    } else if (data.nivel_riesgo === "MEDIO") {
        colorFondo = "#fff3cd";
        icono = "⚠️";
    } else {
        colorFondo = "#d4edda";
        icono = "✅";
    }

    resultadoDiv.style.backgroundColor = colorFondo;

    resultadoDiv.innerHTML = `
        <h3>${icono} Resultado de la predicción</h3>

        <p><strong>Aerolínea:</strong> 
           ${data.aerolinea_nombre} (${data.aerolinea_codigo})
        </p>

        <p><strong>¿Vuelo retrasado?:</strong> 
           ${data.retrasado}
        </p>

        <p><strong>Probabilidad de retraso:</strong> 
           ${data.probabilidad_retraso} %
        </p>

        <p><strong>Nivel de riesgo:</strong> 
           <strong>${data.nivel_riesgo}</strong>
        </p>

        <p><em>${data.mensaje}</em></p>
    `;
}

function mostrarError(error) {

    const resultadoDiv = document.getElementById("resultado");
    resultadoDiv.innerHTML = "";
    resultadoDiv.style.backgroundColor = "transparent";

    // Si vienen errores de validación
    if (error.errors) {
        for (const campo in error.errors) {
            const errorSpan = document.getElementById(`error-${campo}`);
            if (errorSpan) {
                errorSpan.innerText = error.errors[campo];
            }
        }
    } else {
        resultadoDiv.innerHTML = `
            <p style="color:red;">${error.message || "Error inesperado"}</p>
        `;
    }


}



