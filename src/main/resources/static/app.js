function predecir() {

    limpiarErrores();

    const resultadoDiv = document.getElementById("resultado");
    // Añadimos clase para animación suave
    resultadoDiv.classList.remove("result-visible");

    // Spinner moderno temporal
    resultadoDiv.innerHTML = `
        <div style="text-align:center; padding: 20px; color: #666;">
            <i class="fa-solid fa-circle-notch fa-spin"></i> Analizando datos de vuelo...
        </div>
    `;
    resultadoDiv.classList.add("result-visible");

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

    // Configuración de estilos según riesgo
    let colorFondo;
    let colorTexto;
    let colorBorde;
    let icono;
    let badgeColor;

    if (data.nivel_riesgo === "ALTO") {
        colorFondo = "#ffebee";
        colorTexto = "#c62828";
        colorBorde = "#c62828";
        badgeColor = "#d32f2f";
        icono = "fa-triangle-exclamation";
    } else if (data.nivel_riesgo === "MEDIO") {
        colorFondo = "#fff8e1";
        colorTexto = "#f57f17";
        colorBorde = "#ffb300";
        badgeColor = "#fbc02d";
        icono = "fa-stopwatch";
    } else {
        colorFondo = "#e8f5e9";
        colorTexto = "#2e7d32";
        colorBorde = "#43a047";
        badgeColor = "#388e3c";
        icono = "fa-check-circle";
    }

    // Aplicamos estilos dinámicos al div contenedor
    resultadoDiv.style.backgroundColor = colorFondo;
    resultadoDiv.style.color = "#333";

    // Construimos el HTML nuevo con estructura de Tarjeta
    resultadoDiv.innerHTML = `
        <div class="card-result" style="border-left-color: ${colorBorde};">
            <div class="card-header-res">
                <h3 style="color:${colorTexto}; margin:0;">
                    <i class="fa-solid ${icono}"></i> Predicción
                </h3>
                <span class="risk-badge" style="background-color: ${badgeColor};">
                    Riesgo ${data.nivel_riesgo}
                </span>
            </div>

            <div class="card-body-res">
                <p><strong><i class="fa-solid fa-plane"></i> Aerolínea:</strong> 
                   ${data.aerolinea_nombre} <small>(${data.aerolinea_codigo})</small>
                </p>

                <p><strong><i class="fa-solid fa-chart-line"></i> Probabilidad retraso:</strong> 
                   ${data.probabilidad_retraso}%
                </p>

                <p><strong><i class="fa-regular fa-comment-dots"></i> Estado:</strong> 
                   ${data.retrasado ? 'Se espera retraso' : 'A tiempo'}
                </p>

                <p style="margin-top: 10px; font-style: italic; color: #555; border-top: 1px dashed #ccc; padding-top: 10px;">
                   "${data.mensaje}"
                </p>
            </div>
        </div>
    `;

    // Aseguramos que se vea
    resultadoDiv.classList.add("result-visible");
}

function mostrarError(error) {

    const resultadoDiv = document.getElementById("resultado");
    resultadoDiv.innerHTML = "";
    resultadoDiv.style.backgroundColor = "transparent";
    resultadoDiv.classList.remove("result-visible");

    // Si vienen errores de validación
    if (error.errors) {
        for (const campo in error.errors) {
            // Nota: Mantenemos tu lógica de búsqueda de ID
            const errorSpan = document.getElementById(`error-${campo}`);

            // Fix: Tu HTML original tenía un caso especial id="aerolineaError"
            // pero el loop busca error-aerolinea.
            // Agregamos un fallback por si acaso:
            if (errorSpan) {
                errorSpan.innerText = error.errors[campo];
            } else if (campo === 'aerolinea') {
                const aeroSpan = document.getElementById('aerolineaError');
                if(aeroSpan) aeroSpan.innerText = error.errors[campo];
            }
        }
    } else {
        resultadoDiv.innerHTML = `
            <div style="background-color: #ffebee; color: #c62828; padding: 15px; border-radius: 8px; border: 1px solid #ef9a9a;">
                <strong><i class="fa-solid fa-circle-xmark"></i> Error:</strong> ${error.message || "Error inesperado de conexión"}
            </div>
        `;
        resultadoDiv.classList.add("result-visible");
    }
}


